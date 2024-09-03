#/usr/bin/env bash

cargo build --release --package holochain-manager-uniffi

cargo ndk -o ../../tauri-plugin-holochain-foreground-service/android/src/main/jniLibs \
  --manifest-path ./Cargo.toml \
  -t armeabi-v7a \
  -t arm64-v8a \
  -t x86 \
  -t x86_64 \
  build --release

cargo run --bin uniffi-bindgen generate --library ../../../target/release/libholochain_manager_uniffi.so --language kotlin --out-dir ../../tauri-plugin-holochain-foreground-service/android/src/main/java/