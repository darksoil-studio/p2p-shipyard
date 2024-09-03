use std::{
    collections::{BTreeMap, HashMap},
    path::PathBuf,
};

use holochain::prelude::{
    AppBundle, AppBundleError, AppBundleSource, AppManifest, CoordinatorBundle,
    CoordinatorManifest, DnaBundle, DnaError, DnaFile, DnaHash, MembraneProof, NetworkSeed,
    RoleName, UpdateCoordinatorsPayload, ZomeDependency, ZomeError, ZomeLocation, ZomeManifest,
};
use holochain_client::{
    AdminWebsocket, AgentPubKey, AppInfo, ConductorApiError, InstallAppPayload, InstalledAppId,
};
use holochain_conductor_api::{AppInfoStatus, CellInfo};
use holochain_types::web_app::WebAppBundle;
use mr_bundle::{error::MrBundleError, Bundle, ResourceBytes};

use crate::filesystem::FileSystemError;

pub async fn install_web_app(
    admin_ws: &AdminWebsocket,
    app_id: String,
    bundle: WebAppBundle,
    membrane_proofs: HashMap<RoleName, MembraneProof>,
    agent: Option<AgentPubKey>,
    network_seed: Option<NetworkSeed>,
) -> crate::Result<AppInfo> {
    let app_info = install_app(
        admin_ws,
        app_id.clone(),
        bundle.happ_bundle().await?,
        membrane_proofs,
        agent,
        network_seed,
    )
    .await?;

    log::info!("Installed web-app's ui {app_id:?}");

    Ok(app_info)
}

pub async fn install_app(
    admin_ws: &AdminWebsocket,
    app_id: String,
    bundle: AppBundle,
    membrane_proofs: HashMap<RoleName, MembraneProof>,
    agent: Option<AgentPubKey>,
    network_seed: Option<NetworkSeed>,
) -> crate::Result<AppInfo> {
    log::info!("Installing app {}", app_id);

    let agent_key = match agent {
        Some(agent) => agent,
        None => admin_ws
            .generate_agent_pub_key()
            .await
            .map_err(|err| crate::Error::ConductorApiError(err))?,
    };

    let app_info = admin_ws
        .install_app(InstallAppPayload {
            agent_key,
            membrane_proofs,
            network_seed,
            source: AppBundleSource::Bundle(bundle),
            installed_app_id: Some(app_id.clone()),
        })
        .await
        .map_err(|err| crate::Error::ConductorApiError(err))?;
    log::info!("Installed app {app_info:?}");

    let response = admin_ws
        .enable_app(app_id.clone())
        .await
        .map_err(|err| crate::Error::ConductorApiError(err))?;

    log::info!("Enabled app {app_id:?}");

    Ok(response.app)
}

#[derive(Debug, thiserror::Error)]
pub enum UpdateAppError {
    #[error(transparent)]
    AppBundleError(#[from] AppBundleError),

    #[error(transparent)]
    ZomeError(#[from] ZomeError),

    #[error(transparent)]
    MrBundleError(#[from] MrBundleError),

    #[error(transparent)]
    FileSystemError(#[from] FileSystemError),

    #[error(transparent)]
    DnaError(#[from] DnaError),

    #[error("ConductorApiError: `{0:?}`")]
    ConductorApiError(ConductorApiError),

    #[error("Error connecting to the websocket")]
    WebsocketError,

    #[error("The given app was not found: {0}")]
    AppNotFound(String),

    #[error("The role {0} was not found the app {1}")]
    RoleNotFound(RoleName, InstalledAppId),
}

pub async fn update_app(
    admin_ws: &AdminWebsocket,
    app_id: String,
    bundle: AppBundle,
) -> Result<(), UpdateAppError> {
    log::info!(
        "Checking whether the coordinator zomes for app {} need to be updated",
        app_id
    );

    // Get the DNA def from the admin websocket
    let apps = admin_ws
        .list_apps(None)
        .await
        .map_err(|err| UpdateAppError::ConductorApiError(err))?;

    let mut app = apps
        .into_iter()
        .find(|app| app.installed_app_id.eq(&app_id))
        .ok_or(UpdateAppError::AppNotFound(app_id.clone()))?;

    let new_dna_files = resolve_dna_files(bundle).await?;

    let mut updated = false;

    for (role_name, new_dna_file) in new_dna_files {
        let cells = app
            .cell_info
            .remove(&role_name)
            .ok_or(UpdateAppError::RoleNotFound(
                role_name.clone(),
                app.installed_app_id.clone(),
            ))?;

        for cell in cells {
            let mut zomes: Vec<ZomeManifest> = Vec::new();
            let mut resources: Vec<(PathBuf, ResourceBytes)> = Vec::new();

            let dna_hash = match cell {
                CellInfo::Provisioned(c) => c.cell_id.dna_hash().clone(),
                CellInfo::Cloned(c) => c.cell_id.dna_hash().clone(),
                CellInfo::Stem(c) => c.original_dna_hash.clone(),
            };
            let old_dna_def = admin_ws
                .get_dna_definition(dna_hash.clone())
                .await
                .map_err(|err| UpdateAppError::ConductorApiError(err))?;

            for (zome_name, coordinator_zome) in new_dna_file.dna_def().coordinator_zomes.iter() {
                let deps = coordinator_zome
                    .clone()
                    .erase_type()
                    .dependencies()
                    .to_vec();
                let dependencies = deps
                    .into_iter()
                    .map(|name| ZomeDependency { name })
                    .collect();

                if let Some(old_zome_def) = old_dna_def
                    .coordinator_zomes
                    .iter()
                    .find(|(zome, _)| zome.eq(&zome_name))
                {
                    if !old_zome_def
                        .1
                        .wasm_hash(&zome_name)?
                        .eq(&coordinator_zome.wasm_hash(&zome_name)?)
                    {
                        log::info!("Updating coordinator zome {zome_name} for role {role_name}");
                        let resource_path = PathBuf::from(zome_name.0.to_string());
                        zomes.push(ZomeManifest {
                            name: zome_name.clone(),
                            hash: None,
                            dylib: None,
                            location: ZomeLocation::Bundled(resource_path.clone()),
                            dependencies: Some(dependencies),
                        });
                        let wasm = new_dna_file.get_wasm_for_zome(&zome_name)?;
                        resources.push((resource_path, wasm.code().to_vec().into()));
                    }
                } else {
                    log::info!("Adding new coordinator zome {zome_name} for role {role_name}");
                    let resource_path = PathBuf::from(zome_name.0.to_string());
                    zomes.push(ZomeManifest {
                        name: zome_name.clone(),
                        hash: None,
                        dylib: None,
                        location: ZomeLocation::Bundled(resource_path.clone()),
                        dependencies: Some(dependencies),
                    });
                    let wasm = new_dna_file.get_wasm_for_zome(&zome_name)?;
                    resources.push((resource_path, wasm.code().to_vec().into()));
                }
            }

            if !zomes.is_empty() {
                let source: CoordinatorBundle =
                    Bundle::new(CoordinatorManifest { zomes }, resources, PathBuf::from("/"))?
                        .into();
                let req = UpdateCoordinatorsPayload {
                    dna_hash,
                    source: holochain_types::prelude::CoordinatorSource::Bundle(Box::new(source)),
                };

                admin_ws
                    .update_coordinators(req)
                    .await
                    .map_err(|err| UpdateAppError::ConductorApiError(err))?;
                updated = true;
            }
        }
    }

    if updated {
        if let AppInfoStatus::Running = app.status {
            admin_ws
                .disable_app(app_id.clone())
                .await
                .map_err(|err| UpdateAppError::ConductorApiError(err))?;
            admin_ws
                .enable_app(app_id.clone())
                .await
                .map_err(|err| UpdateAppError::ConductorApiError(err))?;
        }
        log::info!("Updated app {app_id:?}");
    }

    Ok(())
}

async fn resolve_dna_files(
    app_bundle: AppBundle,
) -> Result<BTreeMap<RoleName, DnaFile>, UpdateAppError> {
    let mut dna_files: BTreeMap<RoleName, DnaFile> = BTreeMap::new();

    let bundle = app_bundle.into_inner();

    for app_role in bundle.manifest().app_roles() {
        if let Some(location) = app_role.dna.location {
            let (dna_def, _) = resolve_location(&bundle, &location).await?;

            dna_files.insert(app_role.name.clone(), dna_def);
        }
    }

    Ok(dna_files)
}

async fn resolve_location(
    app_bundle: &Bundle<AppManifest>,
    location: &mr_bundle::Location,
) -> Result<(DnaFile, DnaHash), UpdateAppError> {
    let bytes = app_bundle.resolve(location).await?;
    let dna_bundle: DnaBundle = mr_bundle::Bundle::decode(&bytes)?.into();
    let (dna_file, original_hash) = dna_bundle.into_dna_file(Default::default()).await?;
    Ok((dna_file, original_hash))
}
