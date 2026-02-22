package id.xms.omakdroid

import android.app.Application
import android.util.Log

class OmakDroidApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i("OmakDroidApp", "OmakDroid Base Application Online. Pre-warming Rust NDK Contexts...")
    }
}
