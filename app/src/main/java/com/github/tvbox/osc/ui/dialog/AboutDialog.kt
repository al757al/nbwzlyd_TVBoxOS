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
        SpanUtils.with(textView).append("           公众号 安卓哥开发\n\n         ").setBold().setForegroundColor(
            Color.RED
        ).
        append("版本号 V" + BuildConfig.VERSION_NAME + "更新日志:")
            .append("\n").append(
                """  
1. 修复仓库切换后第一条线路无法选择问题
2. 播放页选集后关闭选集弹框
3. 增加历史搜索记录
4. 优化py加载逻辑，按需加载，没有py需求的不加载py框架，提高启动速度
5. 首页交互改版，可以横滑
6. 内置仓库
7. 默认焦点改为首页
8. 去掉jar加载成功的提示
9. 同步q代码
    """.trimIndent()
            ).create()
    }
}