use tauri::{
  plugin::{Builder, TauriPlugin},
  Manager, Runtime,
};

pub use models::*;

#[cfg(desktop)]
mod desktop;
#[cfg(mobile)]
mod mobile;

mod error;
mod models;

pub use error::{Error, Result};

#[cfg(desktop)]
use desktop::HolochainForegroundService;
#[cfg(mobile)]
use mobile::HolochainForegroundService;

/// Extensions to [`tauri::App`], [`tauri::AppHandle`] and [`tauri::Window`] to access the holochain-foreground-service APIs.
pub trait HolochainForegroundServiceExt<R: Runtime> {
  fn holochain_foreground_service(&self) -> &HolochainForegroundService<R>;
}

impl<R: Runtime, T: Manager<R>> crate::HolochainForegroundServiceExt<R> for T {
  fn holochain_foreground_service(&self) -> &HolochainForegroundService<R> {
    self.state::<HolochainForegroundService<R>>().inner()
  }
}

pub fn init<R: Runtime>() -> TauriPlugin<R> {
  Builder::new("holochain-foreground-service")
    .setup(|app, api| {
      #[cfg(mobile)]
      let dialog = mobile::init(app, api)?;
      #[cfg(desktop)]
      let dialog = desktop::init(app, api)?;
      app.manage(dialog);
      Ok(())
    })
    .build()
}