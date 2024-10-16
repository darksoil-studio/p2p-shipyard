use holochain::{conductor::error::ConductorError, prelude::SerializedBytesError};
use holochain_client::ConductorApiError;
use mr_bundle::error::MrBundleError;
use one_err::OneErr;
use serde::{ser::Serializer, Serialize};

use crate::{commands::install_web_app::UpdateAppError, filesystem::FileSystemError};

pub type Result<T> = std::result::Result<T, Error>;

#[derive(Debug, thiserror::Error)]
pub enum Error {
    #[error(transparent)]
    Io(#[from] std::io::Error),

    #[cfg(mobile)]
    #[error(transparent)]
    PluginInvoke(#[from] tauri::plugin::mobile::PluginInvokeError),

    #[error(transparent)]
    LairError(OneErr),

    #[error(transparent)]
    ConductorError(#[from] ConductorError),

    #[error(transparent)]
    SerializedBytesError(#[from] SerializedBytesError),

    #[error(transparent)]
    MrBundleError(#[from] MrBundleError),

    #[error(transparent)]
    TauriError(#[from] tauri::Error),

    #[error(transparent)]
    FileSystemError(#[from] FileSystemError),

    #[error(transparent)]
    CtrclError(#[from] ctrlc::Error),

    #[error("JSON serialization error: {0}")]
    SerdeJsonError(#[from] serde_json::Error),

    #[error("Lock error: {0}")]
    LockError(String),

    #[error(transparent)]
    UrlParseError(#[from] url::ParseError),

    #[error("ConductorApiError: `{0:?}`")]
    ConductorApiError(ConductorApiError),

    #[error("Http server error: {0}")]
    HttpServerError(String),

    #[error("Filesystem error: {0}")]
    FilesystemError(String),

    #[error("Sign zome call error: {0}")]
    SignZomeCallError(String),

    #[error("Admin websocket error: {0}")]
    AdminWebsocketError(String),

    #[error("Error connecting websocket: {0}")]
    WebsocketConnectionError(String),

    #[error("Error opening app: {0}")]
    OpenAppError(String),

    #[error("App \"{0}\" does not exist ")]
    AppDoesNotExist(String),

    #[error("Holochain has not been initialized yet")]
    HolochainNotInitializedError,

    #[error("App \"{0}\" does not have any UI")]
    AppDoesNotHaveUIError(String),

    #[error(transparent)]
    UpdateAppError(#[from] UpdateAppError),
}

impl Serialize for Error {
    fn serialize<S>(&self, serializer: S) -> std::result::Result<S::Ok, S::Error>
    where
        S: Serializer,
    {
        serializer.serialize_str(self.to_string().as_ref())
    }
}
