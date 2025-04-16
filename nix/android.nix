{ inputs, ... }:

{
  perSystem = { inputs', lib, pkgs, self', system, ... }:
    let
      cmakeVersion = "3.22.1";
      # cmakeVersion = "3.10.2";
      sdkPath = "${self'.packages.android-sdk}/libexec/android-sdk";
      ndkPath = "${sdkPath}/ndk-bundle";
      toolchainSystem =
        if pkgs.stdenv.isLinux then "linux-x86_64" else "darwin-x86_64";
      prebuiltPath = "${ndkPath}/toolchains/llvm/prebuilt/${toolchainSystem}";
      toolchainBinsPath = "${prebuiltPath}/bin";

    in rec {
      # packages.android-sdk = inputs.android-nixpkgs.sdk.${system} (sdkPkgs:
      #   with sdkPkgs; [
      #     cmdline-tools-latest
      #     build-tools-34-0-0
      #     build-tools-30-0-3
      #     platform-tools
      #     # ndk-bundle
      #     ndk-25-2-9519653
      #     # ndk-28-0-13004108
      #     platforms-android-34
      #   ]);

      packages.android-sdk = let
        pkgs = import inputs.nixpkgs {
          inherit system;
          config.allowUnfree = true;
          config.android_sdk.accept_license = true;
        };
      in (pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ "24" "34" "35" ];
        systemImageTypes = [ "google_apis_playstore" ];
        abiVersions = [ "armeabi-v7a" "arm64-v8a" "x86" "x86_64" ];
        includeNDK = true;
        ndkVersion = "25.2.9519653";

        # ndkVersion = "28.0.13004108";
        # ndkVersion = "26.0.10792818";
        # ndkVersion = "26.3.11579264";
        # ndkVersion = "24.0.8215888";
        # ndkVersion = "23.0.7344513-rc4";
        # ndkVersion = "29.0.13113456-rc1";
        # ndkVersion = "29.0.13113456";
        # ndkVersion = "28.0.13004108";
        # ndkVersion = "27.2.12479018";
        cmakeVersions = [ cmakeVersion ];
        # includeExtras = [ "extras" "google" "auto" ];
      }).androidsdk;

      devShells.androidDev = pkgs.mkShell {
        packages = [ packages.android-sdk pkgs.gradle pkgs.jdk17 pkgs.aapt ];

        shellHook = ''
          export GRADLE_OPTS="-Dorg.gradle.project.android.aapt2FromMavenOverride=${pkgs.aapt}/bin/aapt2";

          export ANDROID_SDK_ROOT=${sdkPath}
          export ANDROID_HOME=${sdkPath}
          export NDK_HOME=${ndkPath}
          export CMAKE_TOOLCHAIN_FILE=$NDK_HOME/build/cmake/android.toolchain.cmake
        '';
      };

      packages.androidTauriRust = let
        rust = inputs.holonix.packages.${system}.rust.override {
          extensions = [ "rust-src" "rustfmt" ];
          targets = [
            "armv7-linux-androideabi"
            "x86_64-linux-android"
            "i686-linux-android"
            "aarch64-unknown-linux-musl"
            "wasm32-unknown-unknown"
            "x86_64-pc-windows-gnu"
            "x86_64-unknown-linux-musl"
            "x86_64-apple-darwin"
            "aarch64-linux-android"
          ];
        };
        linuxCargo = pkgs.writeShellApplication {
          name = "cargo";
          runtimeInputs = [ rust ];
          text = ''
            RUSTFLAGS="-C link-arg=$(gcc -print-libgcc-file-name)" cargo "$@"
          '';
        };
        androidRust = pkgs.symlinkJoin {
          name = "rust-for-android";
          paths = [
            # linuxCargo
            rust
            packages.android-sdk
          ];
          buildInputs = [ pkgs.makeWrapper ];
          # --set LIBCLANG_PATH ${pkgs.llvmPackages_18.libclang.lib}/lib \
          postBuild = ''
            wrapProgram $out/bin/cargo \
              --set ANDROID_STANDALONE_TOOLCHAIN ${prebuiltPath} \
              --set ANDROID_HOME ${sdkPath} \
              --set ANDROID_SDK_ROOT ${sdkPath} \
              --set ANDROID_NDK ${ndkPath} \
              --set ANDROID_NDK_HOME ${ndkPath} \
              --set ANDROID_NDK_ROOT ${ndkPath} \
              --set CMAKE_GENERATOR Ninja \
              --set CMAKE_TOOLCHAIN_FILE ${ndkPath}/build/cmake/android.toolchain.cmake \
              --set RANLIB ${toolchainBinsPath}/llvm-ranlib \
              --set AR ${toolchainBinsPath}/llvm-ar \
              --set CC_aarch64_linux_android ${toolchainBinsPath}/aarch64-linux-android24-clang \
              --set CXX_aarch64_linux_android ${toolchainBinsPath}/aarch64-linux-android24-clang++ \
              --set CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER ${toolchainBinsPath}/aarch64-linux-android24-clang \
              --set CFLAGS_AARCH64_LINUX_ANDROID "--target=aarch64-linux-android --sysroot=${prebuiltPath}/sysroot" \
              --set CXXFLAGS_AARCH64_LINUX_ANDROID "--target=aarch64-linux-android" \
              --set CC_i686_linux_android ${toolchainBinsPath}/i686-linux-android24-clang \
              --set CXX_i686_linux_android ${toolchainBinsPath}/i686-linux-android24-clang++ \
              --set CARGO_TARGET_I686_LINUX_ANDROID_LINKER ${toolchainBinsPath}/i686-linux-android24-clang \
              --set CFLAGS_I686_LINUX_ANDROID "--target=i686-linux-android --sysroot=${prebuiltPath}/sysroot" \
              --set CXXFLAGS_I686_LINUX_ANDROID "--target=i686-linux-android" \
              --set CC_x86_64_linux_android ${toolchainBinsPath}/x86_64-linux-android24-clang \
              --set CXX_x86_64_linux_android ${toolchainBinsPath}/x86_64-linux-android24-clang++ \
              --set CARGO_TARGET_X86_64_LINUX_ANDROID_LINKER ${toolchainBinsPath}/x86_64-linux-android24-clang \
              --set CFLAGS_X86_64_LINUX_ANDROID "--target=x86_64-linux-android --sysroot=${prebuiltPath}/sysroot" \
              --set CXXFLAGS_X86_64_LINUX_ANDROID "--target=x86_64-linux-android" \
              --set CC_armv7_linux_androideabi ${toolchainBinsPath}/armv7a-linux-androideabi24-clang \
              --set CXX_armv7_linux_androideabi ${toolchainBinsPath}/armv7a-linux-androideabi24-clang++ \
              --set CARGO_TARGET_ARMV7_LINUX_ANDROIDEABI_LINKER ${toolchainBinsPath}/armv7a-linux-androideabi24-clang \
              --set CFLAGS_ARMV7_LINUX_ANDROID "--target=armv7a-linux-androideabi --sysroot=${prebuiltPath}/sysroot" \
              --set CXXFLAGS_ARMV7_LINUX_ANDROID "--target=armv7a-linux-androideabi" \
              --set BINDGEN_EXTRA_CLANG_ARGS_AARCH64_LINUX_ANDROID "--sysroot=${prebuiltPath}/sysroot -I${prebuiltPath}/sysroot/usr/include/aarch64-linux-android" \
              --set BINDGEN_EXTRA_CLANG_ARGS_X86_64_LINUX_ANDROID "--sysroot=${prebuiltPath}/sysroot -I${prebuiltPath}/sysroot/usr/include/x86_64-linux-android" \
              --set BINDGEN_EXTRA_CLANG_ARGS_ARMV7_LINUX_ANDROIDEABI "--sysroot=${prebuiltPath}/sysroot -I${prebuiltPath}/sysroot/usr/include/arm-linux-androideabi" \
              --set BINDGEN_EXTRA_CLANG_ARGS_I686_LINUX_ANDROID "--sysroot=${prebuiltPath}/sysroot -I${prebuiltPath}/sysroot/usr/include/i686-linux-android"
          '';
        };

      in androidRust;

      devShells.holochainTauriAndroidDev = pkgs.mkShell {
        inputsFrom = [
          self'.devShells.tauriDev
          devShells.androidDev
          inputs'.tnesh-stack.devShells.holochainDev
        ];
        packages = [ packages.androidTauriRust ];
        buildInputs =
          inputs.tnesh-stack.outputs.dependencies.${system}.holochain.buildInputs
          ++ (with pkgs; [ glibc_multi rust-bindgen ninja cmake ]);

        # export PATH=${sdkPath}/cmake/${cmakeVersion}/bin:$PATH
        # export OPENSSL_ROOT_DIR=${pkgs.openssl.dev}
        shellHook = ''
          export PS1='\[\033[1;34m\][p2p-shipyard-android:\w]\$\[\033[0m\] '
        '';
      };
    };
}
