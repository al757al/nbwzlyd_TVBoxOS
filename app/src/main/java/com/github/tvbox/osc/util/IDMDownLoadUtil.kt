package com.github.tvbox.osc.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.Gravity
import com.blankj.utilcode.util.ToastUtils
import com.github.tvbox.osc.base.App.Companion.instance

/**
 * <pre>
 *     author : derek
 *     time   : 2022/10/09
 *     desc   :
 *     version:
 * </pre>
 */
class IDMDownLoadUtil {

    private val pkg = "idm.internet.download.manager.plus"


    fun startIDMDownLoad(context: Context) {
        val vodInfo = instance?.getVodInfo()
        if (vodInfo?.seriesMap?.isEmpty() == true || vodInfo?.seriesMap?.get(vodInfo.playFlag)
                .isNullOrEmpty()
        ) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("下载地址获取失败~请视频正常加载(或者开启窗口预览模式)后下载")
            return
        }
        val vodSeries = vodInfo?.seriesMap?.get(vodInfo.playFlag)?.get(vodInfo.playIndex)
        var downLoadUrl = vodSeries?.url ?: return
        if (TextUtils.isEmpty(downLoadUrl) || !downLoadUrl.startsWith("http") || !downLoadUrl.startsWith(
                "https"
            )
        ) {
            downLoadUrl = vodInfo.downLoadUrl
        }
        if (TextUtils.isEmpty(downLoadUrl)) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("下载地址获取失败~请视频正常加载后下载")
            return
        }
        if (downLoadUrl.startsWith("http") || downLoadUrl.startsWith("https")) {
            IDMDownLoadUtil().realStartIDMDownLoad(context, vodInfo.name, downLoadUrl)
        } else {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("下载地址获取失败~请视频正常加载后下载")
            return
        }
    }

    private fun realStartIDMDownLoad(context: Context, fileName: String?, fileUrl: String?) {
        val packageManager = context.packageManager
        try {
            val packageInfo = packageManager.getPackageInfo(pkg, 0)
            if (packageInfo != null) {
                val intent = Intent("android.intent.action.VIEW")
                intent.component = ComponentName(
                    pkg,
                    "idm.internet.download.manager.Downloader"
                )
                intent.putExtra("secure_uri", true)
                intent.data =
                    Uri.parse(fileUrl)
                intent.putExtra(
                    "extra_useragent",
                    UA.randomOne()
                )
                intent.putExtra("extra_filename", fileName)
                context.startActivity(intent)
            }

        } catch (e: Exception) {
            ToastUtils.showShort("请先安装IDM+")
        }
    }
}