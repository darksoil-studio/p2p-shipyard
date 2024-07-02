use anyhow::anyhow;
use clap::Parser;
use holochain_types::{
    app::InstallAppPayload,
    dna::{AgentPubKey, AgentPubKeyB64},
    prelude::AppBundle,
};
use lair_keystore::dependencies::sodoken::{BufRead, BufWrite};
use std::collections::HashMap;
use std::path::PathBuf;
use tauri::{AppHandle, Context, Wry};
use tauri_plugin_holochain::{HolochainExt, HolochainPluginConfig};
use url2::url2;

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
struct Args {
    /// The path of the file tree to modify.
    pub happ_bundle_path: PathBuf,

    /// The bundle identifier for the Tauri app
    #[clap(long)]
    pub password: Option<String>,

    /// The bundle identifier for the Tauri app
    #[clap(long)]
    pub ui_port: String,

    /// The bundle identifier for the Tauri app
    #[clap(long)]
    pub agent_key: Option<String>,

    /// The bundle identifier for the Tauri app
    #[clap(long)]
    pub network_seed: Option<String>,

    /// The bundle identifier for the Tauri app
    #[clap(long)]
    pub signal_url: String,

    /// The bundle identifier for the Tauri app
    #[clap(long)]
    pub bootstrap_url: String,

    /// The bundle identifier for the Tauri app
    #[clap(long)]
    pub conductor_dir: Option<PathBuf>,
}

const APP_ID: &'static str = "example";

fn vec_to_locked(mut pass_tmp: Vec<u8>) -> std::io::Result<BufRead> {
    match BufWrite::new_mem_locked(pass_tmp.len()) {
        Err(e) => {
            pass_tmp.fill(0);
            Err(e.into())
        }
        Ok(p) => {
            {
                let mut lock = p.write_lock();
                lock.copy_from_slice(&pass_tmp);
                pass_tmp.fill(0);
            }
            Ok(p.to_read())
        }
    }
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    let args = Args::parse();

    let conductor_dir = match args.conductor_dir {
        Some(c) => c,
        None => {
            let tmp_dir =
                tempdir::TempDir::new("hc-embark").expect("Could not create temporary directory");
            tmp_dir.into_path()
        }
    };

    let password = args.password.unwrap_or_default();

    let dev_url = url2!("http://localhost:{}", args.ui_port);

    let mut context: Context<Wry> = tauri::generate_context!();
    context.config_mut().build.dev_url = Some(dev_url.into());

    tauri::Builder::default()
        .plugin(
            tauri_plugin_log::Builder::default()
                .level(log::LevelFilter::Warn)
                .build(),
        )
        .plugin(tauri_plugin_holochain::init(
            vec_to_locked(password.as_bytes().to_vec()).expect("Can't build passphrase"),
            HolochainPluginConfig {
                signal_url: url2!("{}", args.signal_url),
                bootstrap_url: url2!("{}", args.bootstrap_url),
                holochain_dir: conductor_dir,
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
                setup(
                    handle.clone(),
                    args.happ_bundle_path,
                    agent_key,
                    HashMap::new(),
                    args.network_seed,
                )
                .await?;

                handle
                    .holochain()?
                    .main_window_builder(String::from("main"), false, Some(APP_ID.into()), None)
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
    agent_key: Option<AgentPubKey>,
    membrane_proofs: HashMap<String, std::sync::Arc<holochain_types::prelude::SerializedBytes>>,
    network_seed: Option<String>,
) -> anyhow::Result<()> {
    let admin_ws = handle.holochain()?.admin_websocket().await?;
    let agent_key = match agent_key {
        Some(agent_key) => agent_key,
        None => {
            let agent_key = admin_ws
                .generate_agent_pub_key()
                .await
                .map_err(|err| anyhow!("Error generating the agent: {:?}", err))?;
            agent_key
        }
    };
    let app_info = admin_ws
        .install_app(InstallAppPayload {
            agent_key,
            membrane_proofs,
            network_seed,
            source: holochain_types::app::AppBundleSource::Path(app_bundle_path),
            installed_app_id: None,
        })
        .await
        .map_err(|err| anyhow!("Error installing the app: {err:?}"))?;
    log::info!("Installed app {app_info:?}");

    let response = admin_ws
        .enable_app(app_info.installed_app_id.clone())
        .await
        .map_err(|err| anyhow!("Error enabling the app: {err:?}"))?;

    Ok(())
}

fn main() {
    run()
}
