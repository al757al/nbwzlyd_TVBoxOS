package com.github.tvbox.osc.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.Gravity
import com.blankj.utilcode.util.ToastUtils

class StringUtils {

    companion object {
        @JvmStatic
        fun isStrEmpty(str: String?): Boolean {
            return str.isNullOrEmpty()
        }

        @JvmStatic
        fun copyText(context: Context?, copyText: String?) {
            //获取剪切板管理器
            val cm = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            //设置内容到剪切板
            cm.setPrimaryClip(ClipData.newPlainText(null, copyText))
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("已复制")

        }

    }
}