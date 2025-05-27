# Device Setup

> [!NOTE] 
> In this guide you will learn how to test your app directly using an Android device connected via USB to your computer.

1. In your Android device, enable the [developer options](https://developer.android.com/studio/debug/dev-options).

2. After you have enabled the developer options, [enable USB debbuging](https://developer.android.com/studio/debug/dev-options#Enable-debugging).

3. Connect your Android device to your computer with a USB cable, and confirm in your Android device that you allow USB debugging from this computer.

4. In the root folder of your repository, run:

```bash
nix develop .#androidDev
```

This is a replacement command for the usual `nix develop`, which includes `Android Studio`, and all the necessary tooling that you need for Android development. Every time you want to test or build for the Android platform, you will need to enter the nix devShell this way and then your command from inside of it.

> [!WARNING]
> The first time this is run, it will take some time. This is because nix has to download and build all the necessary Android tooling. After the first time, it will be almost instant.

5. Inside your `androidDev` devShell, run:

```bash
adb devices
```

If all the previous steps were successful, you should see your device in the list of devices.

---

That's it! You can now take a look at [developing for Android](./developing) to know what commands to use when targeting Android.
