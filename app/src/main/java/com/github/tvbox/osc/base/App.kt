package com.github.tvbox.osc.base

import androidx.multidex.MultiDexApplication
import com.codelang.window.FloatingWindowManager.init
import com.github.tvbox.osc.bean.VodInfo
import com.github.tvbox.osc.startup.*
import com.github.tvbox.osc.util.js.JSEngine
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
        init(this)
        StartupManager.Builder()
            .addStartup(UITask())
            .addStartup(HawkTask())
            .addStartup(DatabaseTask())
            .addStartup(ServerTask())
            .addStartup(PlayerTask())
            .build(this)
            .start().await()
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
