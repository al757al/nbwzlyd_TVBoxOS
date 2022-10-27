package com.github.tvbox.osc.startup

import android.content.Context
import android.text.TextUtils
import com.github.tvbox.osc.data.AppDataManager
import com.github.tvbox.osc.util.HawkConfig
import com.orhanobut.hawk.Hawk
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.executor.ExecutorManager
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import java.util.concurrent.Executor

class DatabaseTask : AndroidStartup<String>() {
    override fun callCreateOnMainThread(): Boolean = false

    override fun create(context: Context): String? {
        //初始化数据库
        AppDataManager.init()
        MMKV.initialize(context)
        CrashReport.initCrashReport(context, "cb38e2920c", false);
        initParams(context)
        return DatabaseTask::class.simpleName

    }

    override fun createExecutor(): Executor {
        return ExecutorManager.instance.cpuExecutor
    }

    override fun waitOnMainThread() = false

    private fun initParams(context: Context) {
        // Hawk
        Hawk.init(context).build()
        if (!Hawk.contains(HawkConfig.PLAY_TYPE)) {
            Hawk.put(HawkConfig.PLAY_TYPE, 1)
        }
        val homeUrl = Hawk.get(HawkConfig.API_URL, "")
        if (TextUtils.isEmpty(homeUrl)) {
            Hawk.put(
                HawkConfig.API_URL,
                "https://agit.ai/nbwzlyd/xiaopingguo/raw/branch/master/yujun.txt"
            )
        }
        if (Hawk.get<String>(HawkConfig.IJK_CODEC).isNullOrEmpty()) {
            Hawk.put(HawkConfig.IJK_CODEC, "硬解码")
        }
    }
}