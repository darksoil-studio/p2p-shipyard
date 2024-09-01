use crate::filesystem::FileSystem;

pub fn pong_iframe() -> String {
    format!("<html><head></head><body><script>window.onload = () => window.parent.postMessage('pong', '*') </script></body></html>")
}

pub async fn read_asset(
    fs: &FileSystem,
    app_id: &String,
    mut asset_name: String,
) -> crate::Result<Option<(Vec<u8>, Option<String>)>> {
    log::debug!("Reading asset from filesystem. Asset name: {}", asset_name);
    if asset_name.starts_with("/") {
        asset_name = asset_name
            .strip_prefix("/")
            .expect("Failed to strip prefix")
            .to_string();
    }
    if asset_name == "" {
        asset_name = String::from("index.html");
    }

    let assets_path = fs.bundle_store.get_ui_path(&app_id)?;
    let asset_file = assets_path.join(asset_name);

    let mime_guess = mime_guess::from_path(asset_file.clone());

    let mime_type = match mime_guess.first() {
        Some(mime) => Some(mime.essence_str().to_string()),
        None => {
            log::warn!("Could not determine MIME Type of file '{:?}'", asset_file);
            None
        }
    };

    match std::fs::read(asset_file.clone()) {
        Ok(asset) => Ok(Some((asset, mime_type))),
        Err(_e) => {
            // Fallback to "index.html" to support push-based client-side routing without hashing
            let asset_file = assets_path.join(String::from("index.html"));
            let mime_guess = mime_guess::from_path(asset_file.clone());

            let mime_type = match mime_guess.first() {
                Some(mime) => Some(mime.essence_str().to_string()),
                None => {
                    log::warn!("Could not determine MIME Type of file '{:?}'", asset_file);
                    None
                }
            };
            match std::fs::read(asset_file.clone()) {
                Ok(asset) => Ok(Some((asset, mime_type))),
                Err(_e) => Ok(None)
            }
        },
    }
}
