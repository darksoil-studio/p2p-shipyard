use std::convert::Infallible;
use std::sync::PoisonError;
use url2::Url2Error;

#[derive(uniffi::Error, thiserror::Error, Debug)]
pub enum HolochainRuntimeFFIError {
    #[error("Holochain Manager Error: {0}")]
    HolochainError(String),

    #[error(transparent)]
    ConfigError(#[from] HolochainRuntimeFFIConfigError),

    #[error("HolochainRuntimeFFI is not launched")]
    HolochainRuntimeFFINotLaunched,

    #[error("Mutex poisoned")]
    PoisonError,

    #[error("IO Error: {0}")]
    IOError(String)
}
impl<T> From<PoisonError<T>> for HolochainRuntimeFFIError {
    fn from(_err: PoisonError<T>) -> Self {
        Self::PoisonError
    }
}


#[derive(uniffi::Error, thiserror::Error, Debug)]
#[uniffi(flat_error)]
pub enum HolochainRuntimeFFIConfigError {
    #[error(transparent)]
    Url2Error(#[from] Url2Error),

    #[error(transparent)]
    Infallible(#[from] Infallible),
}
