use tx5_signal_srv::{exec_tx5_signal_srv, Config, Result, SrvHnd};
use url2::Url2;

pub async fn can_connect_to_signal_server(signal_url: Url2) -> Result<()> {
    tx5_signal::Cli::builder()
        .with_url(signal_url.into())
        .build()
        .await?;

    Ok(())
}

pub async fn run_local_signal_service(local_ip: String, port: u16) -> Result<SrvHnd> {
    let mut config = Config::default();
    config.interfaces = local_ip;
    config.port = port;
    config.demo = false;
    log::info!("Running local signal service {:?}", config);

    let (sig_hnd, _addr_list, _err_list) = exec_tx5_signal_srv(config).await?;
    Ok(sig_hnd)
}
