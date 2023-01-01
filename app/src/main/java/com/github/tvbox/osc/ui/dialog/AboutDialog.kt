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
                    1、直播右上角展示epg信息
                    2、直播地址与线路解耦，直播地址不再根据线路变化。切换线路只会保存直播地址
                    3、修复遥控器模式下直播选中频道后偶现焦点丢失问题
                    4、修复豆瓣显示内容不全问题
                    
    """.trimIndent()
            ).create()
    }
}