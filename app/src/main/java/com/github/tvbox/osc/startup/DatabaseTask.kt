package com.github.tvbox.osc.startup

import android.content.Context
import com.github.tvbox.osc.data.AppDataManager
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
        return DatabaseTask::class.simpleName

    }

    override fun createExecutor(): Executor {
        return ExecutorManager.instance.cpuExecutor
    }

    override fun waitOnMainThread() = false
}