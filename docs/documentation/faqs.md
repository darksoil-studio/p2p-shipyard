# Frequently Asked Questions

## Does this mean that holochain already supports mobile?

Well, not quite. Let's break it down to the two main mobile platforms:

### Android

Holochain has experimental support for Android. This means that holochain works as expected on Android, **except for these issues**:

- [`delete_link` bug in holochain core](https://github.com/holochain/holochain/issues/3901).
- [Every time the Android app is opened, holochain takes ~10 seconds to boot up, so there is a long loading screen](https://github.com/holochain/holochain/issues/3243).
- [Go compiler issue on Android 11 or later](https://github.com/holochain/tx5/issues/87). p2p Shipyard solves this issue by providing a custom go toolchain, which is already included in the `devShells` and scaffolded projects described in throughout this documentation site, so **if you use p2p Shipyard, this issue is not present at all**.

### iOS

In development, holochain works as expected in iOS. But Apple prevents JIT compilation in iOS devices, so when a holochain app is published in the iOS store, it does not work. Thankfully there is already [work in progress done by wasmer](https://github.com/wasmerio/wasmer/issues/4486) to address this issue. Stay tuned for updates!

## Well, okey... Then how does p2p Shipyard help me now?

For now, you can build a desktop end-user hApp that your users can download and use, as all macOS, Linux and Windows are well supported. Furthermore, you can start experimenting with Android support, which has some UX downsides but is workable. After the issues with holochain mobile outlined above are resolved, you will be able to upgrade to a new version of the plugin to automatically get full mobile support in your hApp.

This is the way ourselves at [darksoil.studio](https://darksoil.studio) are building hApps right now. We are monitoring the issues at the core holochain infrastructure level, and in constant communication with the core holochain development team to help get them fixed. We hope that the remaining issues that prevent holochain to work on mobile outlined above get resolved soon, so that we can start deploying our holochain apps to end users.
