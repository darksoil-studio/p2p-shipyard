use std::sync::Arc;

use crate::HolochainRuntime;
use holochain_client::AppInfo;

pub async fn list_apps(holochain: Arc<HolochainRuntime>) -> crate::Result<Vec<AppInfo>> {
    let admin_ws = holochain.admin_websocket().await?;

    let apps = admin_ws
        .list_apps(None)
        .await
        .map_err(|err| crate::Error::ConductorApiError(err))?;

    Ok(apps)
}
