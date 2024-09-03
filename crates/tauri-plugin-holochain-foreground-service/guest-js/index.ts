import { invoke } from '@tauri-apps/api/core'

export async function launch(): Promise<string | null> {
  return await invoke<{value?: string}>('plugin:holochain-foreground-service|launch', {
    payload: {},
  }).then((r) => (r.value ? r.value : null));
}
