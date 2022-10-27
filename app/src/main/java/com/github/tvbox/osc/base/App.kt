package com.github.tvbox.osc.base

import androidx.multidex.MultiDexApplication
import com.blankj.utilcode.util.LogUtils
import com.github.tvbox.osc.bean.VodInfo
import com.github.tvbox.osc.startup.DatabaseTask
import com.github.tvbox.osc.startup.PlayerTask
import com.github.tvbox.osc.startup.ServerTask
import com.github.tvbox.osc.startup.UITask
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
        val startTime = System.currentTimeMillis()
        StartupManager.Builder()
            .addStartup(UITask())
            .addStartup(ServerTask())
            .addStartup(DatabaseTask())
            .addStartup(PlayerTask())
            .build(this)
            .start().await()
        LogUtils.dTag("derek119", "time-->" + (System.currentTimeMillis() - startTime))
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
