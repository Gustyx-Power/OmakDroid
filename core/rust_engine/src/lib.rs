use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use jni::sys::jboolean;
use std::process::Command;

#[no_mangle]
pub extern "system" fn Java_id_xms_omakdroid_NativeEngine_initEngine(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    1
}

#[no_mangle]
pub extern "system" fn Java_id_xms_omakdroid_NativeEngine_executeCommand(
    mut env: JNIEnv,
    _class: JClass,
    command: JObject,
) -> jboolean {
    1
}

#[no_mangle]
pub extern "system" fn Java_id_xms_omakdroid_NativeEngine_pingEngine<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jni::sys::jstring {
    let output = env.new_string("OMAKDROID RUST ENGINE ONLINE! 🦀")
        .expect("Couldn't create java string!");
    output.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_id_xms_omakdroid_NativeEngine_bootLinuxKernel<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    proot_path: JString<'local>,
    rootfs_path: JString<'local>,
    tmp_dir: JString<'local>,
) -> jni::sys::jstring {
    // Convert JString parameters to Rust Strings
    let proot: String = env
        .get_string(&proot_path)
        .expect("Invalid proot path")
        .into();
    let rootfs: String = env
        .get_string(&rootfs_path)
        .expect("Invalid rootfs path")
        .into();
    let tmp: String = env
        .get_string(&tmp_dir)
        .expect("Invalid tmp dir")
        .into();

    // Execute PRoot with the same arguments as the Kotlin version
    let output = Command::new(&proot)
        .env("PROOT_TMP_DIR", &tmp)
        .arg("--link2symlink")
        .arg("-0")
        .arg("-r")
        .arg(&rootfs)
        .arg("-b")
        .arg("/dev")
        .arg("-b")
        .arg("/proc")
        .arg("-b")
        .arg("/sys")
        .arg("-w")
        .arg("/root")
        .arg("/usr/bin/env")
        .arg("-i")
        .arg("HOME=/root")
        .arg("TERM=xterm-256color")
        .arg("PATH=/bin:/usr/bin:/sbin:/usr/sbin")
        .arg("/bin/bash")
        .arg("-c")
        .arg("echo 'OMAKDROID KERNEL (POWERED BY RUST) ONLINE' && echo '---' && uname -a && echo '---' && cat /etc/os-release")
        .output();

    // Format the result
    let result_string = match output {
        Ok(out) => {
            let stdout = String::from_utf8_lossy(&out.stdout);
            let stderr = String::from_utf8_lossy(&out.stderr);
            let exit_code = out.status.code().unwrap_or(-1);
            
            let mut result = String::new();
            result.push_str(&format!("PRoot: {}\n", proot));
            result.push_str(&format!("Rootfs: {}\n", rootfs));
            result.push_str(&format!("Exit Code: {}\n", exit_code));
            result.push_str("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            
            if exit_code == 0 && stdout.contains("OMAKDROID KERNEL (POWERED BY RUST) ONLINE") {
                result.push_str("✓ KERNEL BOOT SUCCESS (RUST ENGINE)\n\n");
                result.push_str(&stdout);
            } else {
                result.push_str("✗ KERNEL BOOT FAILED\n\n");
                result.push_str(&format!("Output:\n{}", stdout));
                if !stderr.is_empty() {
                    result.push_str(&format!("\nErrors:\n{}", stderr));
                }
            }
            
            result
        }
        Err(e) => format!("Rust Execution Failed: {}", e),
    };

    // Convert result to Java string
    let j_result = env
        .new_string(result_string)
        .expect("Failed to create Java string");
    j_result.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_id_xms_omakdroid_NativeEngine_executeLinuxCommand<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    command: JString<'local>,
    proot_path: JString<'local>,
    rootfs_path: JString<'local>,
    tmp_dir: JString<'local>,
) -> jni::sys::jstring {
    // Convert JString parameters to Rust Strings
    let cmd_str: String = env
        .get_string(&command)
        .expect("Invalid command")
        .into();
    let proot: String = env
        .get_string(&proot_path)
        .expect("Invalid proot path")
        .into();
    let rootfs: String = env
        .get_string(&rootfs_path)
        .expect("Invalid rootfs path")
        .into();
    let tmp: String = env
        .get_string(&tmp_dir)
        .expect("Invalid tmp dir")
        .into();

    // Execute the user's command via PRoot
    let output = Command::new(&proot)
        .env("PROOT_TMP_DIR", &tmp)
        .arg("--link2symlink")
        .arg("-0")
        .arg("-r")
        .arg(&rootfs)
        .arg("-b")
        .arg("/dev")
        .arg("-b")
        .arg("/proc")
        .arg("-b")
        .arg("/sys")
        .arg("-w")
        .arg("/root")
        .arg("/usr/bin/env")
        .arg("-i")
        .arg("HOME=/root")
        .arg("TERM=xterm-256color")
        .arg("PATH=/bin:/usr/bin:/sbin:/usr/sbin")
        .arg("USER=root")
        .arg("LOGNAME=root")
        .arg("/bin/bash")
        .arg("-c")
        .arg(&cmd_str)
        .output();

    // Format the result - just return stdout/stderr without extra formatting
    let result_string = match output {
        Ok(out) => {
            let stdout = String::from_utf8_lossy(&out.stdout);
            let stderr = String::from_utf8_lossy(&out.stderr);
            
            let mut result = String::new();
            if !stdout.is_empty() {
                result.push_str(&stdout);
            }
            if !stderr.is_empty() {
                if !result.is_empty() {
                    result.push('\n');
                }
                result.push_str(&stderr);
            }
            
            // If both are empty, return a newline to indicate command completed
            if result.is_empty() {
                result.push('\n');
            }
            
            result
        }
        Err(e) => format!("Error: Failed to execute command: {}\n", e),
    };

    // Convert result to Java string
    let j_result = env
        .new_string(result_string)
        .expect("Failed to create Java string");
    j_result.into_raw()
}
