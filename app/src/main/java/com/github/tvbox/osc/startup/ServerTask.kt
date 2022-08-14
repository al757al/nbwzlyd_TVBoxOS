package com.github.tvbox.osc.startup

import android.content.Context
import com.github.tvbox.osc.util.OkGoHelper
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.executor.ExecutorManager
import java.util.concurrent.Executor

class ServerTask : AndroidStartup<String>() {
    override fun callCreateOnMainThread(): Boolean = false

    override fun create(context: Context): String? {
        // OKGo
        OkGoHelper.init()
        return ServerTask::class.simpleName

    }

    override fun waitOnMainThread() = false
    override fun createExecutor(): Executor {
        return ExecutorManager.instance.cpuExecutor
    }

    override fun dependencies(): List<Class<out Startup<*>>> {
        return listOf(DatabaseTask::class.java)
    }
}