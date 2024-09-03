use serde::de::DeserializeOwned;
use tauri::{plugin::PluginApi, AppHandle, Runtime};

use crate::models::*;

pub fn init<R: Runtime, C: DeserializeOwned>(
  app: &AppHandle<R>,
  _api: PluginApi<R, C>,
) -> crate::Result<HolochainForegroundService<R>> {
  Ok(HolochainForegroundService(app.clone()))
}

/// Access to the holochain-foreground-service APIs.
pub struct HolochainForegroundService<R: Runtime>(AppHandle<R>);

impl<R: Runtime> HolochainForegroundService<R> {
  pub fn launch(&self, payload: HolochainRequest) -> crate::Result<HolochainResponse> {
    Ok(HolochainResponse {
    })
  }
}
