#![allow(non_snake_case)]

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::jstring;

/// Native initialization endpoint for OmakDroid's underlying PRoot subsystem.
#[no_mangle]
pub extern "system" fn Java_id_xms_omakdroid_core_system_NativeBridge_initProotEnvironment<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jstring {
    let internal_status = "OmakDroid PRoot Environment Activated [Rust Subsystem OK]";
    
    // Safety: Allocates a UTF-8 JVM visible string from the Rust statically dispatched internal state.
    let output = env.new_string(internal_status).expect("FATAL: Failed to inject string into JVM HEAP");
    
    output.into_raw()
}
