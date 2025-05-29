# Distribution

p2p Shipyard apps use [tauri](https://tauri.app) as the distribution tooling to create executable apps, for both desktop and mobile.

Desktop support comes out of the box, while android and iOS require a bit more setup.

### Targeting destkop

The best way to create a release build targeting MacOs, Linux and Windows is to use the "release-tauri-happ" github action that was scaffolded with the `scaffold-tauri-happ` and `scaffold-holochain-runtime` commands. 

Whenever you are ready to create a release build, simply create a git tag with the format `v0.1.0`. This will trigger a release workflow for your app, targeting MacOs, Linux and Windows.

Take a closer look at the workflow file at `.github/workflows/release-tauri-app.yaml` to understand the step it takes, and edit it to your particular needs if necessary.

### Targeting Android

Go to [Android Project Setup](/guides/android/project-setup) to get started on targetting Android.
