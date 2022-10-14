package com.github.tvbox.osc.startup

//import com.undcover.freedom.pyramid.PythonLoader
import android.content.Context
import com.github.tvbox.osc.util.PlayerHelper
import com.github.tvbox.osc.util.js.JSEngine
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.executor.ExecutorManager
import java.util.concurrent.Executor


class PlayerTask : AndroidStartup<String>() {
    override fun callCreateOnMainThread(): Boolean = false
    override fun create(context: Context): String? {
        PlayerHelper.init()
        JSEngine.getInstance().create()
//        PythonLoader.getInstance().setApplication(App.instance);
        return PlayerTask::class.simpleName

    }

    override fun waitOnMainThread() = true
    override fun createExecutor(): Executor {
        return ExecutorManager.instance.cpuExecutor
    }
}