package com.github.tvbox.osc.ui.dialog.util

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.bean.MoreSourceBean
import com.github.tvbox.osc.event.RefreshEvent
import com.github.tvbox.osc.ext.findFirst
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter
import com.github.tvbox.osc.ui.dialog.SelectDialogNew
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.KVStorage
import com.github.tvbox.osc.util.UA
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.orhanobut.hawk.Hawk
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject

class SourceLineDialogUtil(private val context: Context) {

    //vip 线路 https://agit.ai/guot54/ygbh/raw/branch/master/PRO.json

    private var DEFAULT_URL = ""
    private val dialog by lazy {
        SelectDialogNew<MoreSourceBean>(context)
    }
    private val mSelectDialogAdapterInterface by lazy {
        SelectDialogAdapterInterface()
    }
    private var select: (() -> Unit)? = null


    init {
        val defaultBean =
            KVStorage.getBean(HawkConfig.CUSTOM_STORE_HOUSE_SELECTED, MoreSourceBean::class.java)
        if (defaultBean != null) {
            DEFAULT_URL = defaultBean.sourceUrl
        }
    }

    fun getData(onSelect: () -> Unit) {
        if (TextUtils.isEmpty(DEFAULT_URL)) {
            ToastUtils.showShort("请先选择一个仓库哦~")
            return
        }


        val req = OkGo.get<String>(DEFAULT_URL).cacheMode(CacheMode.FIRST_CACHE_THEN_REQUEST);
        if (DEFAULT_URL.startsWith("https://gitcode")) {
            req.headers(
                "User-Agent",
                UA.randomOne()
            ).headers("Accept", ApiConfig.requestAccept)
        }
        req.cacheTime(10 * 60 * 60 * 1000).execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>?) {
                inflateData(response, onSelect)
            }

            override fun onCacheSuccess(response: Response<String>?) {
                super.onCacheSuccess(response)
                inflateData(response, onSelect)
            }

            override fun onError(response: Response<String>?) {
                super.onError(response)
                Toast.makeText(
                    context,
                    "接口请求失败${response?.exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    private fun inflateData(
        response: Response<String>?,
        onSelect: () -> Unit,
        isCache: Boolean = false
    ) {
        try {
            val json = JSONObject(response?.body().toString())
            val urls: JSONArray = json.getJSONArray("urls")
            //暂时不开放vip，因为有18+，不适合家庭使用
            if (json.has("vip")) {
                val vips = json.getJSONArray("vip")
            }
            val length = urls.length();
            val data = mutableListOf<MoreSourceBean>()
            for (i in 0 until length) {
                val jsonObj = urls.getJSONObject(i)
                val url = jsonObj.getString("url")
                //屏蔽gitea的线路
                if (url.contains("https://gitea.com/")) {
                    continue
                }
                val moreSourceBean = MoreSourceBean().apply {
                    sourceUrl = jsonObj.getString("url")
                    sourceName = jsonObj.getString("name")
                    isServer = true
                }
                data.add(moreSourceBean)
            }
            val selectUrl = Hawk.get(HawkConfig.API_URL, "")
            val findData = data.findFirst {
                it.sourceUrl == selectUrl
            }
            var select = -1
            findData?.let {
                select = data.indexOf(findData)
            }
            showDialog(data, select, onSelect = onSelect)
        } catch (e: Exception) {
            if (!isCache) {
                Toast.makeText(context, "Json解析失败，有可能仓库被删了", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDialog(list: List<MoreSourceBean>, select: Int, onSelect: () -> Unit) {
        dialog.apply {
            setTip("选择线路")
            setAdapter(mSelectDialogAdapterInterface.apply {
                setSelectCallBack {
                    dialog.dismiss()
                    onSelect.invoke()
                }
            }, AdapterDiffCallBack(dialog.oldItems, list.toMutableList()), list, select)
        }
        dialog.show()
    }

    class SelectDialogAdapterInterface : SelectDialogAdapter.SelectDialogInterface<MoreSourceBean> {

        private var select: (() -> Unit)? = null
        fun setSelectCallBack(select: () -> Unit) {
            this.select = select
        }

        override fun click(moreSourceBea: MoreSourceBean?, pos: Int) {
            //更新源
            Hawk.put(HawkConfig.API_URL, moreSourceBea?.sourceUrl)
            KVStorage.putBean(HawkConfig.API_URL_BEAN, moreSourceBea)
            EventBus.getDefault().post(
                RefreshEvent(
                    RefreshEvent.TYPE_API_URL_CHANGE,
                    moreSourceBea?.sourceName?.ifEmpty { moreSourceBea.sourceUrl }
                )
            )
            select?.invoke()
        }

        override fun getDisplay(moreSourceBea: MoreSourceBean?): String {
            return if (moreSourceBea?.sourceName.isNullOrEmpty()) moreSourceBea?.sourceUrl
                ?: "" else moreSourceBea?.sourceName ?: ""
        }

    }


}