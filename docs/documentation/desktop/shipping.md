# Shipping for desktop platforms

The best way to create a release build targeting MacOs, Linux and Windows is to use the "release-tauri-happ" github action that was scaffolded with the `scaffold-tauri-happ` and `scaffold-holochain-runtime` commands. 

Whenever you are ready to create a release build, simply create a git tag with the format `v0.1.0`. This will trigger a release workflow for your app, targeting MacOs, Linux and Windows.

Take a closer look at the workflow file at `.github/workflows/release-tauri-app.yaml` to understand the step it takes, and edit it to your particular needs if necessary.

---

That's it! You are now ready to ship your tauri app to desktop platforms.
