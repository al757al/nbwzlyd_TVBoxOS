package com.github.tvbox.osc.ui.dialog

import android.content.Context
import android.widget.TextView
import com.blankj.utilcode.util.SpanUtils
import com.github.tvbox.osc.BuildConfig
import com.github.tvbox.osc.R

class AboutDialog(context: Context) : BaseDialog(context) {
    init {
        setContentView(R.layout.dialog_about)
        val textView = findViewById<TextView>(R.id.title1)
        SpanUtils.with(textView).append("版本号 V" + BuildConfig.VERSION_NAME + "更新日志:")
            .append("\n").append(
                """
    1.设置页UI优化，感谢双喜大兄弟
    2.增加清除缓存功能，让你的app重获新生
    3.优化遥控器焦点，点开就能看到我上次选的哪个条目
    4.进度条有颜色了，视频进度一看便知
    5.修复内存泄漏，占用内存少了
    5.优化了线路刷新逻辑，不会闪动了
    7.支持远程推送多仓库链接了~ 
    8.手机支持拖拽对仓库进行排序了(电视不支持)~
    9.首页loading时点击返回可以强制关闭loading了，避免阻塞后续操作。心急的大佬不用一直等待~
    10.修复推送剪切板崩溃~
    """.trimIndent()
            ).create()
    }
}