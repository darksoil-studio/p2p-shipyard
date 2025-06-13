# Setup

1. If you haven't already, [install the nix package manager](https://nixos.org/download/#nix-install-linux) with: 

::: code-group
```bash [Linux]
sh <(curl -L https://nixos.org/nix/install) --daemon
```
```bash [MacOs]
sh <(curl -L https://nixos.org/nix/install)
```
:::

And follow along its instructions and prompts.

2. Add the appropriate nix caches to your environment:

```bash
nix run nixpkgs#cachix use holochain-ci
nix run nixpkgs#cachix use darksoil-studio
```

> [!WARNING]
> p2p Shipyard relies heavily on [`nix`](https://nixos.org/) to achieve reproducible environments. Unfortunately, `nix` does not support Windows. If you have a Windows OS, you will need to [install Windows Subsystem Linux](https://learn.microsoft.com/en-us/windows/wsl/install) and run all the steps in this guide inside of its environment.

## Creating an app with p2p Shipyard

p2p Shipyard apps use [holochain](https://developers.holochain.org) as their underlying distributed platform.

1. If you don't have one already, create a new holochain app with:

```bash
nix run github:darksoil-studio/scaffolding#hc-scaffold-happ -- web-app
```

2. Enter the newly created folder, and add support for cross-platform binaries with:

```bash
nix run github:darksoil-studio/tauri-plugin-holochain#scaffold-tauri-happ
```

---

That's it! Now you have the skeleton of a peer-to-peer app. You can now move on to [designing your app](./designing-your-app.md).
