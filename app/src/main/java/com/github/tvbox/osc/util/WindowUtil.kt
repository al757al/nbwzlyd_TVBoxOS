package com.github.tvbox.osc.util

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
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
            if (!Hawk.get(HawkConfig.FORBID_JAR_DIALOG, true)) {
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
                    if (floatWindowViewByToken.isNotEmpty()) {
                        for (view in floatWindowViewByToken) {
                            if (view is ViewGroup && findLastViewGroup(view)) {
                                break
                            } else {
                                activity.windowManager.removeViewImmediate(view)
                            }
                        }
                        if (showToast) {
                            ToastUtils.make().show("系统监测到有注入弹框，已为你强制关闭")
                        }
                    }
                } catch (e: Exception) {
                }
            }, delayTime ?: 200)
        }

        private fun findLastViewGroup(view: View): Boolean {
            try {
                if (view !is ViewGroup) {
                    return false
                }
                val vg1 = view.getChildAt(0) as ViewGroup
                val vg2 = vg1.getChildAt(0) as ViewGroup
                val vg3 = vg2.getChildAt(0) as ViewGroup
                if (vg3.tag == "common_tips") {
                    return true
                }
            } catch (ex: Exception) {
                return false
            }
            return false
        }
    }


}