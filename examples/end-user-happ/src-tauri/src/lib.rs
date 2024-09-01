use holochain_types::prelude::AppBundle;
use lair_keystore::dependencies::sodoken::{BufRead, BufWrite};
use std::collections::HashMap;
use std::path::PathBuf;
use tauri_plugin_holochain::{HolochainExt, HolochainManagerConfig, WANNetworkConfig};
use url2::Url2;
use tauri::{AppHandle, Listener};

const APP_ID: &'static str = "example";

pub fn example_happ() -> AppBundle {
    let bytes = include_bytes!("../../workdir/forum.happ");
    AppBundle::decode(bytes).expect("Failed to decode example happ")
}

pub fn vec_to_locked(mut pass_tmp: Vec<u8>) -> std::io::Result<BufRead> {
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

fn wan_network_config() -> Option<WANNetworkConfig> {
    // Resolved at compile time to be able to point to local services
    if tauri::is_dev() {
        None
    } else {
        Some(WANNetworkConfig {
            signal_url: url2::url2!("wss://signal.holo.host"),
            bootstrap_url: url2::url2!("https://bootstrap.holo.host")
        })
    }
}

fn holochain_dir() -> PathBuf {
    if tauri::is_dev() {
        #[cfg(target_os = "android")]
        {
            app_dirs2::app_root(
                app_dirs2::AppDataType::UserCache,
                &app_dirs2::AppInfo {
                    name: "studio.darksoil.p2pshipyard",
                    author: "darksoil.studio",
                },
            ).expect("Could not get the UserCache directory")
        }
        #[cfg(not(target_os = "android"))]
        {
            let tmp_dir =
                tempdir::TempDir::new("forum").expect("Could not create temporary directory");

            // Convert `tmp_dir` into a `Path`, destroying the `TempDir`
            // without deleting the directory.
            let tmp_path = tmp_dir.into_path();
            tmp_path
        }
    } else {
        app_dirs2::app_root(
            app_dirs2::AppDataType::UserData,
            &app_dirs2::AppInfo {
                name: "example-forum",
                author: std::env!("CARGO_PKG_AUTHORS"),
            },
        )
        .expect("Could not get app root")
        .join("holochain")
    }
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(
            tauri_plugin_log::Builder::default()
                .level(log::LevelFilter::Warn)
                .build(),
        )
        .plugin(tauri_plugin_holochain::async_init(
            vec_to_locked(vec![]).expect("Can't build passphrase"),
            HolochainManagerConfig {
                holochain_dir: holochain_dir(),
                wan_network_config: wan_network_config(),
            },
        ))
        .setup(|app| {
            let handle = app.handle().clone();
            app.handle().listen("holochain://setup-completed", move |_event| {
                let handle = handle.clone();
                tauri::async_runtime::spawn(async move {
                    setup(handle.clone()).await.expect("Failed to setup");

                    handle
                        .holochain()
                        .expect("Failed to get holochain")
                        .main_window_builder(String::from("main"), false, Some(APP_ID.into()), None).await
                        .expect("Failed to build window")
                        .build()
                        .expect("Failed to open main window");
                });
            });

            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}

// Very simple setup for now:
// - On app start, list installed apps:
//   - If there are no apps installed, this is the first time the app is opened: install our hApp
//   - If there **are** apps:
//     - Check if it's necessary to update the coordinators for our hApp
//       - And do so if it is
//
// You can modify this function to suit your needs if they become more complex
async fn setup(handle: AppHandle) -> anyhow::Result<()> {
    let admin_ws = handle.holochain()?.admin_websocket().await?;

    let installed_apps = admin_ws
        .list_apps(None)
        .await
        .map_err(|err| tauri_plugin_holochain::Error::ConductorApiError(err))?;

    if installed_apps.len() == 0 {
        handle
            .holochain()?
            .install_app(
                String::from(APP_ID),
                example_happ(),
                HashMap::new(),
                None,
                None,
            )
            .await?;

        Ok(())
    } else {
        handle.holochain()?.update_app_if_necessary(
            String::from(APP_ID),
            example_happ()
        ).await?;

        Ok(())
    }
}
