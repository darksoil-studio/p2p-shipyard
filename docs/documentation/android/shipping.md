# Shipping your app for Android

To build a production version of your app, you just need to run this very simple command inside the `androidDev` devShell:

::: code-group
```bash [npm]
nix develop .#androidDev
npm run tauri android build
```

```bash [yarn]
nix develop .#androidDev
yarn tauri android build
```

```bash [pnpm]
nix develop .#androidDev
pnpm tauri android build
```
:::

Take into account that this will compile your app for all the different Android target architectures: it will take a long time.

That's it! When the command finishes, you can copy the resulting `Android App Bundle` and [publish it on the `Google Play Store`](https://developer.android.com/studio/publish), or the release method of your choosing.
