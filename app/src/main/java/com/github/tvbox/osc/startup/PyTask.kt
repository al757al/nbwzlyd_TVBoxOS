package com.github.tvbox.osc.startup

import android.content.Context
import com.github.tvbox.osc.base.App
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.executor.ExecutorManager
import com.undcover.freedom.pyramid.PythonLoader
import java.util.concurrent.Executor

/**
 * <pre>
 *     author : derek
 *     time   : 2022/10/15
 *     desc   :
 *     version:
 * </pre>
 */
class PyTask : AndroidStartup<String>() {
    override fun callCreateOnMainThread(): Boolean = false
    override fun create(context: Context): String? {
        PythonLoader.getInstance().setApplication(App.instance)
        return PlayerTask::class.simpleName

    }

    override fun waitOnMainThread() = true
    override fun createExecutor(): Executor {
        return ExecutorManager.instance.cpuExecutor
    }
}