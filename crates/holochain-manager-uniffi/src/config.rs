use crate::error::HolochainRuntimeFFIConfigError;
use holochain_manager::{HolochainManagerConfig, WANNetworkConfig};
use std::path::PathBuf;
use std::str::FromStr;
use url2::Url2;


#[derive(uniffi::Record)]
pub struct HolochainRuntimeFFIConfig {
    /// URL of bootstrap server
    bootstrap_url: String,

    /// URL of signal server
    signal_url: String,

    /// Path to directory where conductor data will be stored
    holochain_dir: String,
}

impl TryInto<HolochainManagerConfig> for HolochainRuntimeFFIConfig {
    type Error = HolochainRuntimeFFIConfigError;
    fn try_into(self) -> Result<HolochainManagerConfig, Self::Error> {
        Ok(HolochainManagerConfig {
            wan_network_config: Some(WANNetworkConfig {
                bootstrap_url: Url2::try_parse(self.bootstrap_url)?,
                signal_url: Url2::try_parse(self.signal_url)?,
            }),
            holochain_dir: PathBuf::from_str(self.holochain_dir.as_str())?,
        })
    }
}
