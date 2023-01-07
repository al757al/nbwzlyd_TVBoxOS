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
        SpanUtils.with(textView).append("公众号 安卓哥开发").setBold().setForegroundColor(Color.RED)
            .append(BuildConfig.VERSION_NAME + "更新日志:")
            .append("\n").append(
                """  
                    1、升级exo播放器到2.18.2
                    2、优化播放器选择页面焦点展示
                    3、支持播放器切换选择框
                    4、优化直播长按后默认播放器立马全局生效
                    5、直播增加分辨率展示
                    
    """.trimIndent()
            ).create()
    }
}