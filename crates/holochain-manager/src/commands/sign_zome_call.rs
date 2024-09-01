use std::sync::Arc;
use holochain::{
    conductor::api::ZomeCall,
    prelude::{CapSecret, CellId, ExternIO, FunctionName, Timestamp, ZomeCallUnsigned, ZomeName},
};
use holochain_client::AgentPubKey;
use holochain_types::prelude::Signature;
use lair_keystore_api::LairClient;
use serde::Deserialize;
use crate::HolochainRuntime;

pub async fn sign_zome_call(
    holochain: Arc<HolochainRuntime>,
    zome_call_unsigned: ZomeCallUnsignedTauri,
) -> crate::Result<ZomeCall> {
    let zome_call_unsigned_converted: ZomeCallUnsigned = zome_call_unsigned.into();

    let signed_zome_call = sign_zome_call_with_client(
        zome_call_unsigned_converted,
        &holochain
            .conductor_handle
            .keystore()
            .lair_client()
    )
    .await?;

    Ok(signed_zome_call)
}

/// Signs an unsigned zome call with the given LairClient
pub async fn sign_zome_call_with_client(
    zome_call_unsigned: ZomeCallUnsigned,
    client: &LairClient,
) -> crate::Result<ZomeCall> {
    // sign the zome call
    let pub_key = zome_call_unsigned.provenance.clone();
    let mut pub_key_2 = [0; 32];
    pub_key_2.copy_from_slice(pub_key.get_raw_32());

    let data_to_sign = zome_call_unsigned.data_to_sign()?;

    let sig = client
        .sign_by_pub_key(pub_key_2.into(), None, data_to_sign)
        .await
        .map_err(|err| crate::Error::LairError(err))?;

    let signature = Signature(*sig.0);

    let signed_zome_call = ZomeCall {
        cell_id: zome_call_unsigned.cell_id,
        zome_name: zome_call_unsigned.zome_name,
        fn_name: zome_call_unsigned.fn_name,
        payload: zome_call_unsigned.payload,
        cap_secret: zome_call_unsigned.cap_secret,
        provenance: zome_call_unsigned.provenance,
        nonce: zome_call_unsigned.nonce,
        expires_at: zome_call_unsigned.expires_at,
        signature,
    };

    return Ok(signed_zome_call);
}

/// The version of an unsigned zome call that's compatible with the serialization
/// behavior of tauri's IPC channel (serde serialization)
/// nonce is a byte array [u8, 32] because holochain's nonce type seems to
/// have "non-serde" deserialization behavior.
#[derive(Deserialize, Debug)]
pub struct ZomeCallUnsignedTauri {
    pub provenance: AgentPubKey,
    pub cell_id: CellId,
    pub zome_name: ZomeName,
    pub fn_name: FunctionName,
    pub cap_secret: Option<CapSecret>,
    pub payload: ExternIO,
    pub nonce: [u8; 32],
    pub expires_at: Timestamp,
}

impl Into<ZomeCallUnsigned> for ZomeCallUnsignedTauri {
    fn into(self) -> ZomeCallUnsigned {
        ZomeCallUnsigned {
            provenance: self.provenance,
            cell_id: self.cell_id,
            zome_name: self.zome_name,
            fn_name: self.fn_name,
            cap_secret: self.cap_secret,
            payload: self.payload,
            nonce: self.nonce.into(),
            expires_at: self.expires_at,
        }
    }
}

impl Clone for ZomeCallUnsignedTauri {
    fn clone(&self) -> Self {
        ZomeCallUnsignedTauri {
            provenance: self.provenance.clone(),
            cell_id: self.cell_id.clone(),
            zome_name: self.zome_name.clone(),
            fn_name: self.fn_name.clone(),
            cap_secret: self.cap_secret.clone(),
            payload: self.payload.clone(),
            nonce: self.nonce.clone(),
            expires_at: self.expires_at.clone(),
        }
    }
}
