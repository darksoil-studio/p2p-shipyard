use crate::config::HolochainRuntimeFFIConfig;
use crate::error::HolochainRuntimeFFIError;
use crate::types::AppInfoFFI;
use holochain_manager::{HolochainRuntime, launch::launch_holochain_runtime, utils::vec_to_locked};
#[macro_use] extern crate log;
extern crate android_logger;


use log::LevelFilter;
use android_logger::{Config,FilterBuilder};


#[derive(uniffi::Object)]
pub struct HolochainRuntimeFFI {
    runtime: HolochainRuntime,
}

#[uniffi::export]
impl HolochainRuntimeFFI {
    #[uniffi::constructor]
    pub async fn launch(passphrase: Vec<u8>, config: HolochainRuntimeFFIConfig) -> Result<Self, HolochainRuntimeFFIError> {
        android_logger::init_once(
            Config::default()
                .with_max_level(LevelFilter::Trace)
                .with_tag("holochainlog")
                .with_filter(FilterBuilder::new().parse("debug,hello::crate=trace").build()),
        );
        
        
        let runtime = launch_holochain_runtime(
                vec_to_locked(passphrase).map_err(|e| HolochainRuntimeFFIError::IOError(e.to_string()))?,
                config.try_into()?
            )
            .await
            .map_err(|e| HolochainRuntimeFFIError::HolochainError(e.to_string()))?;
        
        Ok(HolochainRuntimeFFI {
            runtime
        })
    }

    pub async fn list_installed_apps(&self) -> Result<Vec<AppInfoFFI>, HolochainRuntimeFFIError> {
        let apps = self.runtime.admin_websocket().await
            .map_err(|e| HolochainRuntimeFFIError::HolochainError(e.to_string()))?
            .list_apps(None).await
            .map_err(|e| HolochainRuntimeFFIError::HolochainError(format!("{:?}", e)))?
            .into_iter()
            .map(|a| a.into())
            .collect();
        
        Ok(apps)
    }

    pub fn get_admin_port(&self) -> u16 {
        self.runtime.admin_port
    }
}
