<script>
  import { invoke } from "@tauri-apps/api/core";

  function launch() {
    invoke('plugin:holochain-foreground-service|launch');
  }
</script>

<main class="container">
  <h1>Welcome to Tauri!</h1>

  <button on:click={launch}>Launch holochain foreground service</button>
</main>
