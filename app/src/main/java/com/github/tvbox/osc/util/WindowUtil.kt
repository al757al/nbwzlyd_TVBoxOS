package com.github.tvbox.osc.util

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.ToastUtils
import com.codelang.window.FloatingWindowManager.getFloatWindowViewByToken
import com.orhanobut.hawk.Hawk

/**
 * <pre>
 *     author : derek
 *     time   : 2022/11/27
 *     desc   :
 *     version:
 * </pre>
 */
class WindowUtil {

    companion object {
        @JvmStatic
        fun closeDialog(activity: Activity?, showToast: Boolean = false, delayTime: Long? = 200) {
            if (!Hawk.get(HawkConfig.FORBID_JAR_DIALOG, false)) {
                return
            }
            if (activity?.isFinishing == true || activity == null) {
                return
            }
            //关闭jar注入的弹框
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val floatWindowViewByToken =
                        getFloatWindowViewByToken(activity)
                    if (!floatWindowViewByToken.isEmpty()) {
                        for (view in floatWindowViewByToken) {
                            activity.windowManager.removeViewImmediate(view)
                        }
                        if (showToast) {
                            ToastUtils.make().show("系统监测到有注入弹框，已为你强制关闭（你没看见是代码执行的太快）")
                        }
                    }
                } catch (e: Exception) {
                }
            }, delayTime ?: 200)
        }
    }
}