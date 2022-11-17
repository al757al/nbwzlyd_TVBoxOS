package com.github.tvbox.osc.ui.dialog

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import com.blankj.utilcode.util.SpanUtils
import com.github.tvbox.osc.BuildConfig
import com.github.tvbox.osc.R

class AboutDialog(context: Context) : BaseDialog(context) {
    init {
        setContentView(R.layout.dialog_about)
        val textView = findViewById<TextView>(R.id.title1)
        SpanUtils.with(textView).append("           公众号 安卓哥开发\n\n").setBold().setForegroundColor(
            Color.RED
        ).
        append("版本号 V" + BuildConfig.VERSION_NAME + "更新日志:")
            .append("\n").append(
                """  
修复2.0的各种bug。具体可参考公众号
    """.trimIndent()
            ).create()
    }
}