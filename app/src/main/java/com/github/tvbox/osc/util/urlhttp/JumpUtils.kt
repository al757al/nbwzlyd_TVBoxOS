package com.github.tvbox.osc.util.urlhttp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.ui.activity.HomeActivity

class JumpUtils {
    /**
     * 强制重启首页，刷新数据
     */
    companion object {
        @JvmStatic
        fun forceRestartHomeActivity(context: Context) {
            ApiConfig.release()
            val intent = Intent(context, HomeActivity::class.java)
            val bundle = Bundle()
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

}