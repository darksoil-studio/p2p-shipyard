use holochain_client::ZomeCall;
use tauri::{command, AppHandle, Runtime};
use holochain_manager::commands::ZomeCallUnsignedTauri;
use crate::HolochainExt;

#[command]
pub(crate) async fn sign_zome_call<R: Runtime>(
    app_handle: AppHandle<R>,
    zome_call_unsigned: ZomeCallUnsignedTauri,
) -> crate::Result<ZomeCall> {
    Ok(holochain_manager::commands::sign_zome_call(
        app_handle.holochain()?.holochain_runtime.clone(), 
        zome_call_unsigned
    ).await?)
}
