package com.github.tvbox.osc.ui.dialog

import android.content.Context
import android.widget.TextView
import com.blankj.utilcode.util.SpanUtils
import com.github.tvbox.osc.BuildConfig
import com.github.tvbox.osc.R

class AboutDialog(context: Context) : BaseDialog(context) {
    init {
        setContentView(R.layout.dialog_about)
        val textView = findViewById<TextView>(R.id.title1)
        SpanUtils.with(textView).append("版本号 V" + BuildConfig.VERSION_NAME + "更新日志:")
            .append("\n").append(
                """    
                    1.修复了点击返回强制关闭loading不生效问题
                    2.修复由于强制关闭loading导致资源下载不完全，每次会加载jar失败的问题
                    3.修复无法退出app的问题
                    强烈建议更新~
    """.trimIndent()
            ).create()
    }
}