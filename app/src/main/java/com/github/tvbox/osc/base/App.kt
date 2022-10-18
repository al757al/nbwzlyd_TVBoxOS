package com.github.tvbox.osc.base

import android.text.TextUtils
import androidx.multidex.MultiDexApplication
import com.github.tvbox.osc.bean.VodInfo
import com.github.tvbox.osc.startup.*
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.js.JSEngine
import com.orhanobut.hawk.Hawk
import com.rousetime.android_startup.StartupManager

/**
 * @author pj567
 * @date :2020/12/17
 * @description:
 */
class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        initParams()
        StartupManager.Builder()
            .addStartup(UITask())
            .addStartup(ServerTask())
            .addStartup(DatabaseTask())
            .addStartup(PlayerTask())
            .addStartup(PyTask())
            .build(this)
            .start().await()
    }

    private fun initParams() {
        // Hawk
        Hawk.init(this).build()
        if (!Hawk.contains(HawkConfig.PLAY_TYPE)) {
            Hawk.put(HawkConfig.PLAY_TYPE, 1)
        }
        val homeUrl = Hawk.get(HawkConfig.API_URL, "")
        if (TextUtils.isEmpty(homeUrl)) {
        }
        if (Hawk.get<String>(HawkConfig.IJK_CODEC).isNullOrEmpty()) {
            Hawk.put(HawkConfig.IJK_CODEC, "硬解码")
        }
    }

    companion object {
        @JvmStatic
        var instance: App? = null
            private set
    }

    override fun onTerminate() {
        super.onTerminate()
        JSEngine.getInstance().destroy()
    }

    private var pyLoadSuccess = false

    fun setPyLoadSuccess(success: Boolean) {
        pyLoadSuccess = success
    }

    fun getPyLoadSuccess(): Boolean {
        return pyLoadSuccess
    }

    private var vodInfo: VodInfo? = null
    fun setVodInfo(vodinfo: VodInfo?) {
        vodInfo = vodinfo
    }

    fun getVodInfo(): VodInfo? {
        return vodInfo
    }
}
