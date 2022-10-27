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
1. 多仓库源，单仓库源支持本地clan配置 
2. 多仓推送不再区分多仓，单仓 和线路，系统自动识别
3. 解除gitea的屏蔽逻辑
4. 支持长按3倍速播放
5. 首页横滑优化，二次确认toast，避免遥控器长按右键导致tab快速切换的问题
6. 视频播放支持截取当前时间为开头、结尾
7. 支持搜索历史记录清空
8. 尝试修复打开历史搜索弹框，可能会弹键盘的问题
9. 首页数据缓存策略变更，首页加载更快
10. 修复播放页后台后切换前台，会重启首页的bug
11. 首页固定三大金刚键，不再隐藏，三大金刚键福音
12. 其他bug修复
    """.trimIndent()
            ).create()
    }
}