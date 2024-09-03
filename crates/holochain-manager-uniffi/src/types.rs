use holochain_conductor_api::AppInfo;

#[derive(uniffi::Record)]
pub struct AppInfoFFI {
  /// The unique identifier for an installed app in this conductor
  pub installed_app_id: String,
}

impl From<AppInfo> for AppInfoFFI {
  fn from(value: AppInfo) -> Self {
      Self {
        installed_app_id: value.installed_app_id,
      }
  }
}