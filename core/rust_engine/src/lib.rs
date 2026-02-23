use jni::JNIEnv;
use jni::objects::{JClass, JObject};
use jni::sys::jboolean;

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
