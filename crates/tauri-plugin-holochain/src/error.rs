use holochain_manager::commands::UpdateAppError;
use serde::{ser::Serializer, Serialize};

pub type Result<T> = std::result::Result<T, Error>;

#[derive(Debug, thiserror::Error)]
pub enum Error {
    #[error(transparent)]
    HolochainManagerError(#[from] holochain_manager::Error),

    #[error(transparent)]
    TauriError(#[from] tauri::Error),
    #[error("Lock error: {0}")]
    LockError(String),

    #[error(transparent)]
    UrlParseError(#[from] url::ParseError),

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
