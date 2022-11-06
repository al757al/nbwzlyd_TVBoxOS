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
1.直播增加快进功能
2.直播默认exo播放器
3.播放页不常用按钮聚合
4.设置页UI改版
5.直播增加二次确认退出，避免意外退出问题
6.直播优化，避免闪退和无效动画刷新
7.增加搜索线程
8.同步q版代码，但不是最新
10.增加沉浸式开关
    """.trimIndent()
            ).create()
    }
}