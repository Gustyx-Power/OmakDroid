use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString, JValue};
use jni::sys::jboolean;
use std::process::{Command, Stdio};
use std::io::{BufRead, BufReader};

#[no_mangle]
pub extern "system" fn Java_id_xms_omakdroid_NativeEngine_initEngine(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    1
}

#[no_mangle]
pub extern "system" fn Java_id_xms_omakdroid_NativeEngine_executeCommand(
    _env: JNIEnv,
    _class: JClass,
    _command: JObject,
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

    // Auto-inject DNS configuration
    let resolv_path = format!("{}/etc/resolv.conf", rootfs);
    let _ = std::fs::write(&resolv_path, "nameserver 8.8.8.8\nnameserver 1.1.1.1\n");

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
) {
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

    // Auto-inject DNS configuration
    let resolv_path = format!("{}/etc/resolv.conf", rootfs);
    let _ = std::fs::write(&resolv_path, "nameserver 8.8.8.8\nnameserver 1.1.1.1\n");

    // Merge stderr to stdout
    let full_cmd = format!("{} 2>&1", cmd_str);

    // Spawn process with piped stdout for streaming
    let mut child = match Command::new(&proot)
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
        .arg(&full_cmd)
        .stdout(Stdio::piped())
        .stderr(Stdio::null())
        .spawn() {
            Ok(child) => child,
            Err(e) => {
                // Send error message via callback
                let error_msg = format!("Error: Failed to execute command: {}\n", e);
                if let Ok(j_str) = env.new_string(&error_msg) {
                    let class = env.find_class("id/xms/omakdroid/NativeEngine").ok();
                    if let Some(cls) = class {
                        let _ = env.call_static_method(
                            cls,
                            "appendOutput",
                            "(Ljava/lang/String;)V",
                            &[JValue::Object(&j_str.into())]
                        );
                    }
                }
                return;
            }
        };

    // Get stdout handle
    if let Some(stdout) = child.stdout.take() {
        let reader = BufReader::new(stdout);
        
        // Find the NativeEngine class for callbacks
        let class = match env.find_class("id/xms/omakdroid/NativeEngine") {
            Ok(cls) => cls,
            Err(_) => return,
        };

        // Stream output line by line
        for line in reader.lines() {
            if let Ok(l) = line {
                // Create Java string and call callback
                if let Ok(j_str) = env.new_string(&l) {
                    let _ = env.call_static_method(
                        &class,
                        "appendOutput",
                        "(Ljava/lang/String;)V",
                        &[JValue::Object(&j_str.into())]
                    );
                }
            }
        }
    }

    // Wait for process to complete
    let _ = child.wait();
}
