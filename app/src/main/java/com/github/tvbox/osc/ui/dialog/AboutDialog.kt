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
                    1、悬浮窗支持上下集，支持直播悬浮窗以及切换频道
                    2、增加自动保存网盘开关，默认不再保存配置文件中的网盘
                    3、默认线程12
                    4、exo播放器增加ffmpeg扩展，解决部分视频exo播放无声问题
                    5、exo字幕支持，有bug，暂时不修
                    6、增加阿里云播放器支持
                    7、修复长按主页按钮清除缓存后提示一直存在问题
                    8、悬浮窗以及直播部分UI优化
                    9、理论支持安卓4.3及以上版本，以下版本不再支持

    """.trimIndent()
            ).create()
    }
}