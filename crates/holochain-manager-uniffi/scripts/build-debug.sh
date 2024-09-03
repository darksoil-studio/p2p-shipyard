#/usr/bin/env bash

cargo build --package holochain-manager-uniffi

cargo ndk -o ../../tauri-plugin-holochain-foreground-service/android/src/main/jniLibs \
  --manifest-path ./Cargo.toml \
  -t armeabi-v7a \
  -t arm64-v8a \
  -t x86 \
  -t x86_64 \
  build

cargo run --bin uniffi-bindgen generate --library ../../../target/debug/libholochain_manager_uniffi.so --language kotlin --out-dir ../../tauri-plugin-holochain-foreground-service/android/src/main/java/