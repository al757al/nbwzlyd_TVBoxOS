package com.github.tvbox.osc.ui.dialog

import android.content.Context
import android.widget.TextView
import com.blankj.utilcode.util.SpanUtils
import com.github.tvbox.osc.R
import com.github.tvbox.osc.util.HawkConfig
import com.orhanobut.hawk.Hawk

class DownloadTipsDialog(context: Context) : BaseDialog(context) {
    init {
        setContentView(R.layout.common_tips)
        val textView = findViewById<TextView>(R.id.text2)
        SpanUtils.with(textView).append(
            """  
                    长按剧集页可以下载视频了，赶快看看吧~
                    记得先安装IDM+

    """.trimIndent()
        ).create()

        findViewById<TextView>(R.id.button_ok).setOnClickListener {
            this.dismiss()
        }
        setOnDismissListener {
            Hawk.put(HawkConfig.Had_show_download_tips, true)
        }
    }
}