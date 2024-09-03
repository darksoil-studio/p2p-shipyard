use crate::HolochainExt;
use holochain_client::AppInfo;
use tauri::{command, AppHandle, Runtime};

#[command]
pub(crate) async fn list_apps<R: Runtime>(app: AppHandle<R>) -> crate::Result<Vec<AppInfo>> {
    Ok(holochain_manager::commands::list_apps(app.holochain()?.holochain_runtime.clone()).await?)
}
