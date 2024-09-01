use std::{
    collections::HashMap,
    path::PathBuf,
    sync::Arc,
};

use async_std::sync::Mutex;
use lair_signer::LairAgentSignerWithProvenance;
use holochain::{
    conductor::ConductorHandle,
    prelude::{AppBundle, MembraneProof, NetworkSeed, RoleName},
};
use holochain_client::{AdminWebsocket, AgentPubKey, AppInfo, AppWebsocket, InstalledAppId};
use holochain_types::{web_app::WebAppBundle, websocket::AllowedOrigins};
use tx5_signal_srv::SrvHnd;
use url2::Url2;

pub mod commands;
pub mod config;
pub mod error;
mod features;
pub mod filesystem;
pub mod http_server;
pub mod lair_signer;
pub mod launch;

use commands::{install_app, install_web_app, update_app, UpdateAppError};
pub use error::{Error, Result};
use filesystem::{AppBundleStore, BundleStore, FileSystem};


#[derive(Clone)]
pub struct AppWebsocketAuth {
    pub app_id: String,
    pub main_window: bool,
    pub app_websocket_port: u16,
    pub token: Vec<u8>,
}

pub struct HolochainRuntime {
    pub filesystem: FileSystem,
    pub apps_websockets_auths: Arc<Mutex<Vec<AppWebsocketAuth>>>,
    pub admin_port: u16,
    pub(crate) conductor_handle: ConductorHandle,
    pub(crate) _signal_handle: Option<SrvHnd>,
}


impl HolochainRuntime {
    /// Builds an `AdminWebsocket` ready to use
    pub async fn admin_websocket(&self) -> crate::Result<AdminWebsocket> {
        let admin_ws =
            AdminWebsocket::connect(format!("localhost:{}", self.admin_port))
                .await
                .map_err(|err| crate::Error::WebsocketConnectionError(format!("{err:?}")))?;
        Ok(admin_ws)
    }

    pub async fn get_app_websocket_auth(
        &self,
        app_id: &InstalledAppId,
        main_window: bool,
        allowed_origins: AllowedOrigins,
    ) -> crate::Result<AppWebsocketAuth> {
        let mut apps_websockets_auths = self.apps_websockets_auths.lock().await;
        let existing_auth = apps_websockets_auths
            .iter()
            .find(|auth| auth.main_window == main_window && auth.app_id.eq(app_id));
        if let Some(app_websocket_auth) = existing_auth {
            return Ok(app_websocket_auth.clone());
        }

        let admin_ws = self.admin_websocket().await?;

        let app_port = admin_ws
            .attach_app_interface(0, allowed_origins, Some(app_id.clone()))
            .await
            .map_err(|err| crate::Error::ConductorApiError(err))?;

        let response = admin_ws
            .issue_app_auth_token(
                holochain_conductor_api::IssueAppAuthenticationTokenPayload {
                    installed_app_id: app_id.clone(),
                    expiry_seconds: 999999999,
                    single_use: false,
                },
            )
            .await
            .map_err(|err| crate::Error::ConductorApiError(err))?;

        let token = response.token;

        let app_websocket_auth = AppWebsocketAuth {
            app_id: app_id.clone(),
            main_window,
            app_websocket_port: app_port,
            token,
        };

        apps_websockets_auths.push(app_websocket_auth.clone());
        Ok(app_websocket_auth)
    }

    /// Builds an `AppWebsocket` for the given app ready to use
    ///
    /// * `app_id` - the app to build the `AppWebsocket` for
    pub async fn app_websocket(&self, app_id: InstalledAppId, allowed_origins: AllowedOrigins) -> crate::Result<AppWebsocket> {
        let app_websocket_auth = self.get_app_websocket_auth(&app_id, false, allowed_origins).await?;
        let app_ws = AppWebsocket::connect(
            format!("localhost:{}", app_websocket_auth.app_websocket_port),
            app_websocket_auth.token,
            Arc::new(LairAgentSignerWithProvenance::new(Arc::new(
                self
                    .conductor_handle
                    .keystore()
                    .lair_client()
                    .clone(),
            ))),
        )
        .await
        .map_err(|err| crate::Error::WebsocketConnectionError(format!("{err:?}")))?;

        Ok(app_ws)
    }

    /// Install the given `WebAppBundle` in the holochain runtime
    /// It installs the hApp in the holochain conductor, and extracts the UI for it to be opened using `Self::web_happ_window_builder()`
    ///
    /// * `app_id` - the app id to give to the installed app
    /// * `web_app_bundle` - the web-app bundle to install
    /// * `membrane_proofs` - the input membrane proofs for the app
    /// * `agent` - the agent to install the app for
    /// * `network_seed` - the network seed for the app
    pub async fn install_web_app(
        &self,
        app_id: InstalledAppId,
        web_app_bundle: WebAppBundle,
        membrane_proofs: HashMap<RoleName, MembraneProof>,
        agent: Option<AgentPubKey>,
        network_seed: Option<NetworkSeed>,
    ) -> crate::Result<AppInfo> {
        self.filesystem
            .bundle_store
            .store_web_happ_bundle(app_id.clone(), &web_app_bundle)
            .await?;

        let admin_ws = self.admin_websocket().await?;
        let app_info = install_web_app(
            &admin_ws,
            app_id.clone(),
            web_app_bundle,
            membrane_proofs,
            agent,
            network_seed,
        )
        .await?;

        Ok(app_info)
    }

    /// Install the given `AppBundle` in the holochain conductor
    ///
    /// * `app_id` - the app id to give to the installed app
    /// * `app_bundle` - the web-app bundle to install
    /// * `membrane_proofs` - the input membrane proofs for the app
    /// * `agent` - the agent to install the app for
    /// * `network_seed` - the network seed for the app
    pub async fn install_app(
        &self,
        app_id: InstalledAppId,
        app_bundle: AppBundle,
        membrane_proofs: HashMap<RoleName, MembraneProof>,
        agent: Option<AgentPubKey>,
        network_seed: Option<NetworkSeed>,
    ) -> crate::Result<AppInfo> {
        let admin_ws = self.admin_websocket().await?;

        self.filesystem
            .bundle_store
            .store_happ_bundle(app_id.clone(), &app_bundle)?;

        let app_info = install_app(
            &admin_ws,
            app_id.clone(),
            app_bundle,
            membrane_proofs,
            agent,
            network_seed,
        )
        .await?;

        Ok(app_info)
    }

    /// Updates the coordinator zomes and UI for the given app with an updated `WebAppBundle`
    ///
    /// * `app_id` - the app to update
    /// * `web_app_bundle` - the new version of the web-hApp bundle
    pub async fn update_web_app(
        &self,
        app_id: InstalledAppId,
        web_app_bundle: WebAppBundle,
    ) -> crate::Result<()> {
        self.filesystem
            .bundle_store
            .store_web_happ_bundle(app_id.clone(), &web_app_bundle)
            .await?;

        let admin_ws = self
            .admin_websocket()
            .await
            .map_err(|_err| UpdateAppError::WebsocketError)?;
        update_app(
            &admin_ws,
            app_id.clone(),
            web_app_bundle.happ_bundle().await?,
        )
        .await?;

        Ok(())
    }

    /// Updates the coordinator zomes for the given app with an updated `AppBundle`
    ///
    /// * `app_id` - the app to update
    /// * `app_bundle` - the new version of the hApp bundle
    pub async fn update_app(
        &self,
        app_id: InstalledAppId,
        app_bundle: AppBundle,
    ) -> std::result::Result<(), UpdateAppError> {
        let mut admin_ws = self
            .admin_websocket()
            .await
            .map_err(|_err| UpdateAppError::WebsocketError)?;
        let app_info = update_app(&mut admin_ws, app_id.clone(), app_bundle).await?;

        Ok(app_info)
    }

    /// Checks whether it is necessary to update the hApp, and if so,
    /// updates the coordinator zomes for the given app with an updated `AppBundle`
    ///
    /// To do the check it compares the hash of the `AppBundle` that was installed for the given `app_id`
    /// with the hash of the `current_app_bundle`, and proceeds to update the coordinator zomes for the app if they are different
    ///
    /// * `app_id` - the app to update
    /// * `current_app_bundle` - the new version of the hApp bundle
    pub async fn update_app_if_necessary(
        &self,
        app_id: InstalledAppId,
        current_app_bundle: AppBundle,
    ) -> crate::Result<()> {
        let hash = AppBundleStore::app_bundle_hash(&current_app_bundle)?;

        let installed_apps = self
            .filesystem
            .bundle_store
            .installed_apps_store
            .get()?;
        let Some(installed_app_info) = installed_apps.get(&app_id) else {
            return Err(crate::UpdateAppError::AppNotFound(app_id))?;
        };

        if !installed_app_info.happ_bundle_hash.eq(&hash) {
            self.update_app(app_id, current_app_bundle).await?;
        }

        Ok(())
    }

    /// Checks whether it is necessary to update the web-hApp, and if so,
    /// updates the coordinator zomes and the UI for the given app with an updated `WebAppBundle`
    ///
    /// To do the check it compares the hash of the `WebAppBundle` that was installed for the given `app_id`
    /// with the hash of the `current_web_app_bundle`, and proceeds to update the coordinator zomes and the UI for the app if they are different
    ///
    /// * `app_id` - the app to update
    /// * `current_web_app_bundle` - the new version of the hApp bundle
    pub async fn update_web_app_if_necessary(
        &self,
        app_id: InstalledAppId,
        current_web_app_bundle: WebAppBundle,
    ) -> crate::Result<()> {
        let hash = BundleStore::web_app_bundle_hash(&current_web_app_bundle)?;

        let installed_apps = self
            .filesystem
            .bundle_store
            .installed_apps_store
            .get()?;
        let Some(installed_app_info) = installed_apps.get(&app_id) else {
            return Err(crate::UpdateAppError::AppNotFound(app_id))?;
        };

        if !installed_app_info.happ_bundle_hash.eq(&hash) {
            self.update_web_app(app_id, current_web_app_bundle).await?;
        }

        Ok(())
    }
}

pub struct WANNetworkConfig {
    pub bootstrap_url: Url2,
    pub signal_url: Url2,
}

pub struct HolochainManagerConfig {
    /// If `None`, no WAN networking will take place, only mDNS based networking
    /// Peers in the same LAN will still be able to communicate with each other
    pub wan_network_config: Option<WANNetworkConfig>,
    pub holochain_dir: PathBuf,
}
