package com.github.tvbox.osc.base

import androidx.multidex.MultiDexApplication
import com.github.tvbox.osc.startup.DatabaseTask
import com.github.tvbox.osc.startup.PlayerTask
import com.github.tvbox.osc.startup.ServerTask
import com.github.tvbox.osc.startup.UITask
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.LOG
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
//        initParams()
        val time = System.currentTimeMillis()
        StartupManager.Builder()
            .addStartup(UITask())
            .addStartup(ServerTask())
            .addStartup(DatabaseTask())
            .addStartup(PlayerTask())
            .build(this)
            .start().await()
        LOG.e("COSTEnd   " + (System.currentTimeMillis() - time))
//        val time = System.currentTimeMillis()
//        initParams()
//        // OKGo
//        OkGoHelper.init()
//        // 初始化Web服务器
//        ControlManager.init(this)
//        //初始化数据库
//        AppDataManager.init()
//        MMKV.initialize(this)
//        LOG.e("COST-1   " + (System.currentTimeMillis() - time))
//        LoadSir.beginBuilder()
//            .addCallback(EmptyCallback())
//            .addCallback(LoadingCallback())
//            .commit()
//        AutoSizeConfig.getInstance().setCustomFragment(true).unitsManager
//            .setSupportDP(false)
//            .setSupportSP(false).supportSubunits = Subunits.MM
//        LOG.e("COST0   " + (System.currentTimeMillis() - time))
//        PlayerHelper.init()
//        LOG.e("COSTEnd   " + (System.currentTimeMillis() - time))
    }

    private fun initParams() {
        // Hawk
        Hawk.init(this).build()
        Hawk.put(HawkConfig.DEBUG_OPEN, false)
        if (!Hawk.contains(HawkConfig.PLAY_TYPE)) {
            Hawk.put(HawkConfig.PLAY_TYPE, 1)
        }
    }

    companion object {
        @JvmStatic
        var instance: App? = null
            private set
    }
}