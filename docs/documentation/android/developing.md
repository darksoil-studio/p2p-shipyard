# Developing for Android

While developing a hApp, it's not that useful to just have one agent to test your hApp with. Instead, you usually need a couple of peers to be able to interact with one another. 

The scaffolding setup steps in [how to create an end-user hApp](../how-to-create-an-end-user-happ) and [how to create a holochain runtime](../how-to-create-a-holochain-runtime) create a new script in the top level `package.json` file called `network:android`. This script runs an agent in your local computer and another in an Android device, and enables communication between them. 

Since we want to develop for the Android platform, we need to be inside the `androidDev` devShell:

```bash
nix develop .#androidDev
```

Run all the following commands inside this terminal shell.

Before running the `network:android` command, make sure your Android device is connected to your computer via a USB cable and accessible to the tauri tooling by running:

```bash
adb devices
```

You should see your device listed in the output of that command.

**Also make sure that your Android device is in the same wifi network as your computer**. This is necessary for them to see each other and connect in a small holochain network.

> [!NOTE]
> If you haven't setup your Android device yet, head over to [Android device setup](./device-setup) to do so.

We are now ready to run the command:

::: code-group
```bash [npm]
npm run network:android
```

```bash [yarn]
yarn network:android
```

```bash [pnpm]
pnpm network:android
```
:::

If you want to see logs coming from your rust backend, you can run this command:

```bash
adb logcat
```

---

That's it! Have fun while building your hApp. When you are satisfied with what you have and want to release a new production version of your app, go to [Shipping](./shipping).
