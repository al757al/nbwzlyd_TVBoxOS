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
1.直播增加收藏功能
2.增加集数分组
3.直播源支持clan本地配置
4.备份显示优化
5.支持仓库备份还原，并自动重启app
6.支持alist网盘等功能 同步taka版本，感谢优秀开发者
7.调整再按一次退出直播toast字体大小
8.默认沉浸式开关开启，优化首页布局
10.同步q版代码，ijk优化等
    """.trimIndent()
            ).create()
    }
}