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
                    该版本主要是针对1.0.16版本的bugfix。功能参考1.0.16发版日志
1. 修复倍速问题
2. 亮度默认
3. 修正片尾逻辑
4. 搜索线程改为8
5. 修复多仓地址报错后，缓存的仓库丢失问题

    """.trimIndent()
            ).create()
    }
}