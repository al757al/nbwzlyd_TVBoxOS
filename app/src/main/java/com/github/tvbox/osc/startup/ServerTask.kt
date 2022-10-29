package com.github.tvbox.osc.startup

import android.content.Context
import com.github.tvbox.osc.base.App
import com.github.tvbox.osc.util.OkGoHelper
import com.lzy.okgo.OkGo
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.rousetime.android_startup.executor.ExecutorManager
import java.util.concurrent.Executor

class ServerTask : AndroidStartup<String>() {
    override fun callCreateOnMainThread(): Boolean = false

    override fun create(context: Context): String? {
        // OKGo
        OkGo.getInstance().init(App.instance)
        OkGoHelper.init(context)
        return ServerTask::class.simpleName

    }

    override fun waitOnMainThread() = false
    override fun createExecutor(): Executor {
        return ExecutorManager.instance.cpuExecutor
    }

    override fun dependencies(): List<Class<out Startup<*>>> {
        return listOf(HawkTask::class.java, DatabaseTask::class.java)
    }
}