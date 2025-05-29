# Developing for Android

While developing a peer-to-peer, it's not that useful to just have one node to test your app with. Instead, you usually need a couple of peers to be able to interact with one another.

In the root folder of your app, run: 

::: code-group
```bash [npm]
npm run start
```

```bash [yarn]
yarn start
```

```bash [pnpm]
pnpm start
```
:::

This will start two nodes in your computer.

Now **open another terminal**. Since we want to develop for the Android platform, we need to be inside the `androidDev` devShell.

In the new terminal, run:

```bash
nix develop .#androidDev
```

Run all the following commands inside this terminal shell.

Before starting the Android peer, make sure your Android device is connected to your computer via a USB cable and accessible to the tauri tooling by running:

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
npm run tauri android dev
```

```bash [yarn]
yarn tauri android dev
```

```bash [pnpm]
pnpm tauri android dev
```
:::

This will take a few minutes the first time you run it.

If you want to see logs coming from your rust backend, you can run this command:

```bash
adb logcat
```

---

That's it! Have fun while building your app. When you are satisfied with what you have and want to release a new production version of your app, you can move on to [Shipping](./shipping).
