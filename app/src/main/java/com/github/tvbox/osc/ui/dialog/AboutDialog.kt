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
                    1、与2.0.2及以下版本共存
                    2、修复部分电视明明已经有了存储权限却依然备份失败的问题，
                    3、修复聚合模式下点击全部搜索会闪退的问题。
                    4、优化网盘逻辑，配置地址中的网盘自动写入数据库
                    5、电视端屏蔽idm下载按钮，手机端不受影响
                    6、增加仓库和线路拷贝功能
                    7、优化播放页的焦点选中态，默认会选中当前播放的条目
                    8、增加idm长按下载功能
                    9、线路弹框支持保存推送的配置地址，并支持删除
                    10、修复网盘内置字幕不展示问题
                    11、屏蔽jar注入弹框

    """.trimIndent()
            ).create()
    }
}