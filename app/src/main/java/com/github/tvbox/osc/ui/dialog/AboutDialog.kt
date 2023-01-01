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
                    1、直播/影视增加边缘切换模式，可在边缘滑动切换频道/上下集了
                    2、影视，直播播放器解耦，直播长按播放器可单独设置直播播放器
                    3、选集模式优化，避免过早消失
                    4、优化直播线路偶现的错乱问题
                    5、其他UI小优化
    """.trimIndent()
            ).create()
    }
}