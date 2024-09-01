#[derive(Debug, thiserror::Error)]
pub enum UpdateAppError {
  #[error(transparent)]
  UpdateAppError(#[from] holochain_manager::commands::UpdateAppError),
    
  #[error(transparent)]
  TauriError(#[from] tauri::Error),
}
