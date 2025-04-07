use anyhow::anyhow;
use clap::Parser;
use holochain_client::AppInfo;
use holochain_types::{
    app::{InstallAppPayload, RoleSettings},
    dna::{AgentPubKey, AgentPubKeyB64},
};
use std::collections::HashMap;
use std::path::PathBuf;
use tauri::{AppHandle, Context, Wry};
use tauri_plugin_holochain::{vec_to_locked, HolochainExt, HolochainPluginConfig, NetworkConfig};
use url2::url2;

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
struct Args {
    /// The path of the file tree to modify.
    pub happ_bundle_path: PathBuf,

    /// The password to protect the conductor by.
    #[clap(long)]
    pub password: Option<String>,

    /// The port where the UI server is running.
    #[clap(long)]
    pub ui_port: String,

    /// The admin port to bind the admin interface to.
    #[clap(long)]
    pub admin_port: Option<u16>,

    /// The agent key to install the app with.
    #[clap(long)]
    pub agent_key: Option<String>,

    /// The network seed to install the app with.
    #[clap(long)]
    pub network_seed: Option<String>,

    /// The signal URL to connect to.
    #[clap(long)]
    pub signal_url: Option<String>,

    /// The bootstrap URL to connect to.
    #[clap(long)]
    pub bootstrap_url: Option<String>,

    /// The directory where the conductor directories will be created.
    /// By default a new folder in the /tmp directory.
    #[clap(long)]
    pub conductor_dir: Option<PathBuf>,
}

fn main() {
    let args = Args::parse();

    let conductor_dir = match args.conductor_dir {
        Some(c) => c,
        None => {
            let tmp_dir =
                tempdir::TempDir::new("hc-pilot").expect("Could not create temporary directory");
            tmp_dir.into_path()
        }
    };

    let password = args.password.unwrap_or_default();

    let dev_url = url2!("http://localhost:{}", args.ui_port);

    let mut context: Context<Wry> = tauri::generate_context!();
    context.config_mut().build.dev_url = Some(dev_url.into());

    let mut network_config = NetworkConfig::default();

    match (args.signal_url, args.bootstrap_url) {
        (Some(signal_url), Some(bootstrap_url)) => {
            network_config.signal_url = url2!("{}", signal_url);
            network_config.bootstrap_url = url2!("{}", bootstrap_url);
        }
        (Some(_), None) => {
            panic!("Invalid arguments: --signal-url was provided without --bootstrap-url")
        }
        (None, Some(_)) => {
            panic!("Invalid arguments: --bootstrap-url was provided without --signal-url")
        }
        (None, None) => {}
    };

    tauri::Builder::default()
        .plugin(
            tauri_plugin_log::Builder::default()
                .level(log::LevelFilter::Warn)
                .build(),
        )
        .plugin(tauri_plugin_holochain::init(
            vec_to_locked(password.as_bytes().to_vec()),
            HolochainPluginConfig {
                network_config,
                holochain_dir: conductor_dir,
                admin_port: args.admin_port,
                fallback_to_lan_only: true
            },
        ))
        .setup(|app| {
            let agent_key = match args.agent_key {
                Some(key) => {
                    let key_b64 = AgentPubKeyB64::from_b64_str(key.as_str())?;
                    Some(AgentPubKey::from(key_b64))
                }
                None => None,
            };
            let handle = app.handle();
            let result: anyhow::Result<()> = tauri::async_runtime::block_on(async move {
                let app_info = setup(
                    handle.clone(),
                    args.happ_bundle_path,
                    None,
                    agent_key,
                    args.network_seed,
                )
                .await?;

                handle
                    .holochain()?
                    .main_window_builder(
                        String::from("main"),
                        false,
                        Some(app_info.installed_app_id),
                        None,
                    )
                    .await?
                    .build()?;

                Ok(())
            });
            result?;

            Ok(())
        })
        .run(context)
        .expect("error while running tauri application");
}

async fn setup(
    handle: AppHandle,
    app_bundle_path: PathBuf,
    roles_settings: Option<HashMap<String, RoleSettings>>,
    agent_key: Option<AgentPubKey>,
    network_seed: Option<String>,
) -> anyhow::Result<AppInfo> {
    let admin_ws = handle.holochain()?.admin_websocket().await?;
    let app_info = admin_ws
        .install_app(InstallAppPayload {
            agent_key,
            roles_settings,
            network_seed,
            source: holochain_types::app::AppBundleSource::Path(app_bundle_path),
            installed_app_id: None,
            ignore_genesis_failure: false,
            allow_throwaway_random_agent_key: false,
        })
        .await
        .map_err(|err| anyhow!("Error installing the app: {err:?}"))?;
    log::info!("Installed app {app_info:?}");

    let response = admin_ws
        .enable_app(app_info.installed_app_id.clone())
        .await
        .map_err(|err| anyhow!("Error enabling the app: {err:?}"))?;

    Ok(response.app)
}
