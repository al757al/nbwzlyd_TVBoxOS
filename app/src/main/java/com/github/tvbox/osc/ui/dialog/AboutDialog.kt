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
                    1、首页数据源增加动态列数
                    2、首页数据源推送适配刚刚接口
                    3、焦点问题优化
                    4、网盘内置字幕格式兼容，解决部分字幕无法加载问题
                    5、网盘缓存时间改为2min
                    6、支持alistV3网盘
                    7、支持直播地址复制
                    8、jar弹框文案提示优化
                    9、修复直播有密码时无法进行线路切换操作问题
                    10、支持锁屏

    """.trimIndent()
            ).create()
    }
}