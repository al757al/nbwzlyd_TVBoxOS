package com.github.tvbox.osc.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.github.UA
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.base.App.Companion.instance
import com.github.tvbox.osc.bean.VodInfo
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.AbsCallback
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.Executors

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

    private val spThreadPool = Executors.newSingleThreadExecutor()
    private val mHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun startIDMDownLoad(context: Context) {
        val vodInfo = instance?.getVodInfo()
        if (vodInfo?.seriesMap?.isEmpty() == true || vodInfo?.seriesMap?.get(vodInfo.playFlag)
                .isNullOrEmpty()
        ) {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0)
                .show("下载地址获取失败~请视频正常加载(或者开启窗口预览模式)后下载")
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

    fun startIDMDownLoad(context: Context, vodInfo: VodInfo?, vodSeries: VodInfo.VodSeries) {
        getPlay(
            context,
            vodInfo?.sourceKey,
            vodInfo?.playFlag,
            vodSeries.url,
            vodInfo?.name + vodSeries.name
        )

    }

    // playerContent
    private fun getPlay(
        context: Context,
        sourceKey: String?,
        playFlag: String?,
        url: String?,
        name: String?,
    ) {
        val sourceBean = ApiConfig.get().getSource(sourceKey)
        when (sourceBean.type) {
            3 -> {
                spThreadPool.execute {
                    val sp = ApiConfig.get().getCSP(sourceBean)
                    val json = sp.playerContent(playFlag, url, ApiConfig.get().vipParseFlags)
                    WindowUtil.closeDialog(ActivityUtils.getTopActivity(), false, 0L);
                    try {
                        val result = JSONObject(json)
                        val url = result.get("url").toString()
                        mHandler.post {
//                            WindowUtil.closeDialog(context as?Activity,false,0L)
                            realStartIDMDownLoad(context, name, url)
                        }
                    } catch (th: Throwable) {
                        th.printStackTrace()
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("解析失败，无法下载")
                    }
                }
            }
            0, 1 -> {
                val result = JSONObject()
                try {
                    result.put("key", url)
                    val playUrl = sourceBean.playerUrl.trim { it <= ' ' }
                    if (DefaultConfig.isVideoFormat(url) && playUrl.isEmpty()) {
                        result.put("parse", 0)
                        result.put("url", url)
                    } else {
                        result.put("parse", 1)
                        result.put("url", url)
                    }
                    result.put("playUrl", playUrl)
                    val url = result.get("url").toString()
                    mHandler.post {
//                        WindowUtil.closeDialog(context as?Activity,false,0L)
                        realStartIDMDownLoad(context, name, url)
                    }
                } catch (th: Throwable) {
                    th.printStackTrace()
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("解析失败，无法下载")
                }
            }
            4 -> {
                OkGo.get<String>(sourceBean.api)
                    .params("play", url)
                    .params("flag", playFlag)
                    .tag("play")
                    .execute(object : AbsCallback<String?>() {
                        @Throws(Throwable::class)
                        override fun convertResponse(response: Response): String {
                            return if (response.body != null) {
                                response.body!!.string()
                            } else {
                                throw IllegalStateException("网络请求错误")
                            }
                        }

                        override fun onSuccess(response: com.lzy.okgo.model.Response<String?>) {
                            val json = response.body()
                            LOG.i(json)
                            try {
                                val result = JSONObject(json)
                                result.put("key", url)
                                if (!result.has("flag")) result.put("flag", playFlag)
                                val url = result.get("url").toString()
                                mHandler.post {
//                                    WindowUtil.closeDialog(context as?Activity,false,0L)
                                    realStartIDMDownLoad(context, name, url)
                                }
                            } catch (th: Throwable) {
                                th.printStackTrace()
                                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("解析失败，无法下载")
                            }
                        }

                        override fun onError(response: com.lzy.okgo.model.Response<String?>) {
                            super.onError(response)
                            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("解析失败，无法下载")
                        }
                    })
            }
            else -> {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("解析失败，无法下载")
            }
        }
    }
}