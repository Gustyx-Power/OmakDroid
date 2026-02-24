# Rust Engine JNI Build Instructions

## Overview For Developers

The Rust Engine now handles the PRoot execution at a native level, providing better performance and lower-level control over the Linux kernel boot process.

## Automatic Rootfs Patching

The Rust Engine automatically applies silent fixes before every PRoot execution to prevent common Android kernel errors:

1. **DNS Configuration** - Injects `/etc/resolv.conf` with Google DNS (8.8.8.8) and Cloudflare DNS (1.1.1.1)
2. **ldconfig Dummy** - Creates `/sbin/ldconfig` stub to prevent Android kernel permission errors
3. **dpkg-maintscript-helper Dummy** - Creates `/usr/bin/dpkg-maintscript-helper` stub to prevent dpkg errors
4. **GPG Keyring Auto-Copy** - Copies Ubuntu archive keyring to `/etc/apt/trusted.gpg.d/` to fix NO_PUBKEY errors

These patches are applied silently on every boot and command execution, so end-users never need to manually fix these issues.

## Building the Rust Library

Developer's need to compile the Rust code into a `.so` file for Android. Here are your options:

### Option 1: Using cargo-ndk (Recommended)

```bash
# Install cargo-ndk
cargo install cargo-ndk

# Navigate to the Rust project
cd core/rust_engine

# Build for ARM64 (most modern Android devices)
cargo ndk -t arm64-v8a -o ../../app/src/main/jniLibs build --release

# Optional: Build for other architectures
cargo ndk -t armeabi-v7a -o ../../app/src/main/jniLibs build --release
cargo ndk -t x86_64 -o ../../app/src/main/jniLibs build --release
```

### Option 2: Using Android Studio with CMake

1. Create `app/CMakeLists.txt`:
```cmake
cmake_minimum_required(VERSION 3.22.1)
project(rust_engine)

add_library(rust_engine SHARED IMPORTED)
set_target_properties(rust_engine PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/librust_engine.so)
```

2. Update `app/build.gradle.kts` to include:
```kotlin
android {
    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
        }
    }
}
```

### Option 3: Manual Cross-Compilation

```bash
# Add Android targets
rustup target add aarch64-linux-android

# Set up NDK environment variables
export ANDROID_NDK_HOME=/path/to/ndk
export CC_aarch64_linux_android=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android21-clang
export AR_aarch64_linux_android=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ar

# Build
cd core/rust_engine
cargo build --target aarch64-linux-android --release

# Copy to jniLibs
cp target/aarch64-linux-android/release/librust_engine.so ../../app/src/main/jniLibs/arm64-v8a/
```

## Expected Output

After building and running the app, the Desktop screen should display:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
RUST ENGINE STATUS
OMAKDROID RUST ENGINE ONLINE! 🦀
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
KERNEL INITIALIZATION
PRoot: /data/app/.../lib/arm64/libproot.so
Rootfs: /data/user/0/.../files/rootfs
Exit Code: 0
━━━━━━━━━━━━━━━━━━━━━━━━━━━━

✓ KERNEL BOOT SUCCESS (RUST ENGINE)

OMAKDROID KERNEL (POWERED BY RUST) ONLINE
---
Linux localhost 5.x.x-android #1 SMP PREEMPT ...
---
NAME="Ubuntu"
VERSION="24.03 LTS (Noble Numbat)"
...
```

## Troubleshooting

- If you see "Rust Engine Error: ...", check Android Studio's Logcat for the UnsatisfiedLinkError details
- Ensure the `.so` file is in the correct architecture folder (arm64-v8a for most devices)
- Verify the JNI function signatures match exactly:
  - `Java_id_xms_omakdroid_core_engine_NativeEngine_pingEngine`
  - `Java_id_xms_omakdroid_core_engine_NativeEngine_bootLinuxKernel`
  - `Java_id_xms_omakdroid_core_engine_NativeEngine_executeLinuxCommand`
- Check that PRoot binary exists at `app/src/main/jniLibs/arm64-v8a/libproot.so`
- Verify rootfs was extracted during BIOS initialization
