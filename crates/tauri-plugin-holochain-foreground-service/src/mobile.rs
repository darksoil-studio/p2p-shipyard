use serde::de::DeserializeOwned;
use tauri::{
  plugin::{PluginApi, PluginHandle},
  AppHandle, Runtime,
};

use crate::models::*;

#[cfg(target_os = "android")]
const PLUGIN_IDENTIFIER: &str = "com.plugin.holochainforegroundservice";

#[cfg(target_os = "ios")]
tauri::ios_plugin_binding!(init_plugin_holochain-foreground-service);

// initializes the Kotlin or Swift plugin classes
pub fn init<R: Runtime, C: DeserializeOwned>(
  _app: &AppHandle<R>,
  api: PluginApi<R, C>,
) -> crate::Result<HolochainForegroundService<R>> {
  #[cfg(target_os = "android")]
  let handle = api.register_android_plugin(PLUGIN_IDENTIFIER, "HolochainPlugin")?;
  #[cfg(target_os = "ios")]
  let handle = api.register_ios_plugin(init_plugin_holochain-foreground-service)?;
  Ok(HolochainForegroundService(handle))
}

/// Access to the holochain-foreground-service APIs.
pub struct HolochainForegroundService<R: Runtime>(pub PluginHandle<R>);

impl<R: Runtime> HolochainForegroundService<R> {
  pub fn launch(&self, payload: HolochainRequest) -> crate::Result<HolochainResponse> {
    self
      .0
      .run_mobile_plugin("launch", payload)
      .map_err(Into::into)
  }
}
