# Rust Engine JNI Build Instructions

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
[kernel boot output...]
```

## Troubleshooting

- If you see "Rust Engine Error: ...", check Android Studio's Logcat for the UnsatisfiedLinkError details
- Ensure the `.so` file is in the correct architecture folder (arm64-v8a for most devices)
- Verify the JNI function signature matches exactly: `Java_id_xms_omakdroid_NativeEngine_pingEngine`
