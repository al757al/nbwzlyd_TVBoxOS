package com.github.tvbox.osc.base

import android.text.TextUtils
import androidx.multidex.MultiDexApplication
import com.github.tvbox.osc.bean.VodInfo
import com.github.tvbox.osc.startup.DatabaseTask
import com.github.tvbox.osc.startup.PlayerTask
import com.github.tvbox.osc.startup.ServerTask
import com.github.tvbox.osc.startup.UITask
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.js.JSEngine
import com.orhanobut.hawk.Hawk
import com.rousetime.android_startup.StartupManager
import com.tencent.bugly.crashreport.CrashReport

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
            .build(this)
            .start().await()
//        EpgNameFuzzyMatch.init()
        JSEngine.getInstance().create()
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
        CrashReport.initCrashReport(this, "cb38e2920c", false);
        Hawk.init(this).build()
        if (!Hawk.contains(HawkConfig.PLAY_TYPE)) {
            Hawk.put(HawkConfig.PLAY_TYPE, 1)
        }
        val homeUrl = Hawk.get(HawkConfig.API_URL, "")
        if (TextUtils.isEmpty(homeUrl)) {
//            Hawk.put(
//                HawkConfig.API_URL,
//                "https://agit.ai/nbwzlyd/xiaopingguo/raw/branch/master/xiaopingguo/xiaopingguo.json"
//            )
        }
        Hawk.put(HawkConfig.IJK_CODEC, "硬解码");
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

    private var vodInfo: VodInfo? = null
    fun setVodInfo(vodinfo: VodInfo?) {
        vodInfo = vodinfo
    }

    fun getVodInfo(): VodInfo? {
        return vodInfo
    }
}
