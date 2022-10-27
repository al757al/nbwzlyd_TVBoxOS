package com.github.tvbox.osc.ui.dialog

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.tvbox.osc.R
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.bean.MoreSourceBean
import com.github.tvbox.osc.event.RefreshEvent
import com.github.tvbox.osc.ext.letGone
import com.github.tvbox.osc.ext.letVisible
import com.github.tvbox.osc.ext.removeFirstIf
import com.github.tvbox.osc.server.ControlManager
import com.github.tvbox.osc.ui.activity.HomeActivity
import com.github.tvbox.osc.ui.activity.SettingActivity
import com.github.tvbox.osc.ui.dialog.util.AdapterDiffCallBack
import com.github.tvbox.osc.ui.dialog.util.MyItemTouchHelper
import com.github.tvbox.osc.ui.dialog.util.SourceLineDialogUtil
import com.github.tvbox.osc.ui.tv.QRCodeGen
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.KVStorage
import com.github.tvbox.osc.util.UA
import com.github.tvbox.osc.util.urlhttp.JumpUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.orhanobut.hawk.Hawk
import com.owen.tvrecyclerview.widget.TvRecyclerView
import me.jessyan.autosize.utils.AutoSizeUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

//多源地址
class SourceStoreDialog(private val activity: Activity) : BaseDialog(activity) {
    private var mRecyclerView: TvRecyclerView? = null
    private var mAddMoreBtn: TextView? = null
    private var mLastSelectBean: MoreSourceBean? = null
    private var mSourceNameEdit: EditText? = null
    private var mSourceUrlEdit: EditText? = null
    private var mQrCode: ImageView? = null
    private var mLoading: ProgressBar? = null
    private val mAdapter: MoreSourceAdapter by lazy {
        MoreSourceAdapter()
    }

    override fun show() {
        EventBus.getDefault().register(this)
        super.show()
    }

    override fun dismiss() {
        EventBus.getDefault().unregister(this)
        //更新成最新的仓库排序
        KVStorage.putList(HawkConfig.CUSTOM_STORE_HOUSE, mAdapter.data)
        super.dismiss()
    }

    private var DEFAULT_STORE_URL = "https://gitcode.net/wzlyd1/00/-/raw/master/000.txt"

    private val DEFAULT_DATA = LinkedHashMap<String, MoreSourceBean>()

    init {
        setContentView(R.layout.more_source_dialog_select)
        DEFAULT_STORE_URL = KVStorage.getString(HawkConfig.STORE_HOUSE_URL, DEFAULT_STORE_URL)
            ?: ""
        mRecyclerView = findViewById(R.id.list)
        mAddMoreBtn = findViewById(R.id.inputSubmit)
        mSourceNameEdit = findViewById(R.id.input_sourceName)
        mSourceUrlEdit = findViewById(R.id.input_source_url)
        mAddMoreBtn = findViewById(R.id.inputSubmit)
        mQrCode = findViewById(R.id.qrCode)
        mLoading = findViewById(R.id.play_loading)
        mRecyclerView?.adapter = mAdapter
        mAddMoreBtn?.setOnClickListener {
            val sourceUrl0 = mSourceUrlEdit?.text.toString()
            val sourceName0 = mSourceNameEdit?.text.toString()
            if (sourceUrl0.isEmpty()) {
                Toast.makeText(this@SourceStoreDialog.context, "请输入仓库地址！", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            handleRemotePush(RefreshEvent(RefreshEvent.TYPE_STORE_PUSH).apply {
                this.obj = MoreSourceBean().apply {
                    this.sourceName = sourceName0
                    this.sourceUrl = sourceUrl0
                }
            })

        }
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.tvDel -> {
                    deleteItem(position)
                }
                R.id.tvName -> {
                    selectItem(position)

                }
            }
        }
        refeshQRcode()
        if (DEFAULT_STORE_URL.startsWith("http") || DEFAULT_STORE_URL.startsWith("https")) {
            getMutiSource()
        } else {
            inflateCustomSource(mutableListOf())
        }
    }

    private fun saveCustomSourceBean(sourceUrl0: String?, sourceName0: String?) {
        if (!checkUrlIsValid(sourceUrl0)) {
            return
        }
        var lineSource = sourceUrl0
        if (lineSource?.startsWith("clan://") == true) {
            lineSource = ApiConfig.clanToAddress(lineSource)
        }
        val saveList =
            KVStorage.getList(HawkConfig.CUSTOM_STORE_HOUSE, MoreSourceBean::class.java)
        val sourceBean = MoreSourceBean().apply {
            this.sourceUrl = lineSource.toString()
            this.sourceName = sourceName0?.ifEmpty { "自用仓库" + saveList.size }.toString()
            this.isServer = false
        }
        if (!saveList.contains(sourceBean)) {
            mAdapter.addData(sourceBean)
            mRecyclerView?.scrollToPosition(0)
            saveList.add(sourceBean)
            KVStorage.putList(HawkConfig.CUSTOM_STORE_HOUSE, saveList)
        }
        mSourceUrlEdit?.setText("")
        mSourceNameEdit?.setText("")
    }


    private fun getMutiSource(moreSourceBean: MoreSourceBean? = null) {
        mLoading.letVisible()
        if (DEFAULT_STORE_URL.startsWith("clan://")) {
            DEFAULT_STORE_URL = ApiConfig.clanToAddress(DEFAULT_STORE_URL)
        }
        val req = OkGo.get<String>(DEFAULT_STORE_URL)
            .cacheMode(CacheMode.IF_NONE_CACHE_REQUEST)
        if (DEFAULT_STORE_URL.startsWith("https://gitcode")) {
            req.headers(
                "User-Agent",
                UA.randomOne()
            ).headers("Accept", ApiConfig.requestAccept);
        }

        req.cacheTime(10L * 60L * 60L * 1000).execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>?) {
                serverString2Json(moreSourceBean, response)
            }

            override fun onCacheSuccess(response: Response<String>?) {
                super.onCacheSuccess(response)
                serverString2Json(moreSourceBean, response)
            }

            override fun onError(response: Response<String>?) {
                super.onError(response)
                mLoading.letGone()
                Toast.makeText(
                    context,
                    "接口拉取失败" + response?.exception?.message + "将使用缓存",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    private fun serverString2Json(moreSourceBean: MoreSourceBean?, response: Response<String>?) {
        try {
            mLoading.letGone()
            val jsonObj = JSONObject(response?.body() ?: return)
            var jsonArray: JSONArray? = null
            if (!jsonObj.has("storeHouse")) {
                if (jsonObj.has("urls")) {//可能是单仓库
                    saveCustomSourceBean(
                        moreSourceBean?.sourceUrl ?: "",
                        moreSourceBean?.sourceName ?: ""
                    )
                } else if (jsonObj.has("sites")) {//可能是线路
                    Hawk.put(HawkConfig.API_URL, moreSourceBean?.sourceUrl)
                    val history = Hawk.get(HawkConfig.API_HISTORY, ArrayList<String>())
                    if (!history.contains(moreSourceBean?.sourceUrl)) {
                        history.add(0, moreSourceBean?.sourceUrl.toString())
                    }
                    if (history.size > 20) history.removeAt(20)
                    Hawk.put(HawkConfig.API_HISTORY, history)
                    ToastUtils.showShort("系统识别到你推送的可能是线路，已经帮你保存并重启首页")
                    JumpUtils.forceRestartHomeActivity(context)
                    this.dismiss()
                } else {//无法识别了
                    val text =
                        SpanUtils().append("你的仓库格式不对\n请参考公众号").append(" <仓库定义规则> ")
                            .setBold()
                            .setForegroundColor(Color.RED).append("文章").create()
                    ToastUtils.showShort(text)
                }
            } else {
                jsonArray = jsonObj.getJSONArray("storeHouse")
                if (!response.isFromCache) {
                    KVStorage.putString(HawkConfig.STORE_HOUSE_URL, DEFAULT_STORE_URL)
                }
            }
            for (i in 0 until (jsonArray?.length() ?: 0)) {
                val childJsonObj = jsonArray?.getJSONObject(i)
                val sourceName = childJsonObj?.optString("sourceName")
                val sourceUrl = childJsonObj?.optString("sourceUrl")
                val sourceBean = DEFAULT_DATA[sourceUrl]
                if (sourceBean == null) {
                    val moreSourceBeanNew = MoreSourceBean().apply {
                        this.sourceName = childJsonObj?.optString("sourceName") ?: ""
                        this.sourceUrl = childJsonObj?.optString("sourceUrl") ?: ""
                        this.isServer = true
                    }
                    DEFAULT_DATA[moreSourceBeanNew.sourceUrl] = moreSourceBeanNew
                } else {
                    sourceBean.sourceName = sourceName ?: ""
                    sourceBean.sourceUrl = sourceUrl ?: ""
                    DEFAULT_DATA[sourceBean.sourceUrl] = sourceBean
                }
            }
            val result = DEFAULT_DATA.filter {
                !KVStorage.getBoolean(it.value.sourceUrl, false)
            }.map {
                it.value
            }.toMutableList()

            inflateCustomSource(result)

        } catch (e: Exception) {
            Toast.makeText(context, "JSON解析失败${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun inflateCustomSource(serverResult: MutableList<MoreSourceBean>) {
        val localData = KVStorage.getList(HawkConfig.CUSTOM_STORE_HOUSE, MoreSourceBean::class.java)
        if (localData.isEmpty() && serverResult.isNotEmpty()) {//如果本地保存的是空的，就把新的结果放进去
            localData.addAll(serverResult)
        } else {//否则进行匹配，只保存本地没有的
            val customMap = localData.associateBy { it.uniKey }
            val newResultMap = serverResult.associateBy { it.uniKey }
            newResultMap.forEach {
                if (customMap[it.key] == null) {
                    localData.add(it.value)
                }
            }
        }
        val lastSelectBean =
            KVStorage.getBean(
                HawkConfig.CUSTOM_STORE_HOUSE_SELECTED,
                MoreSourceBean::class.java
            )
        var index = 0
        localData.forEach {
            if (it.sourceUrl != lastSelectBean?.sourceUrl) {
                it.isSelected = false
            } else {
                it.isSelected = true
                index = localData.indexOf(it)
            }
        }

        val diffResult =
            DiffUtil.calculateDiff(AdapterDiffCallBack(mAdapter.data, localData), false)
        //为了适配diffUtil才这么写的
        mAdapter.data.clear()
        mAdapter.data.addAll(localData)
        diffResult.dispatchUpdatesTo(mAdapter)
        if (index != -1) {
            mRecyclerView?.post {
                mRecyclerView?.selectedPosition = index
                mRecyclerView?.scrollToPosition(index)
            }
        }
        ItemTouchHelper(MyItemTouchHelper(mAdapter.data, mAdapter)).attachToRecyclerView(
            mRecyclerView
        )

    }


    //删除仓库地址
    private fun deleteItem(position: Int) {
        val deleteData = mAdapter.data[position]
        val custom = KVStorage.getList(HawkConfig.CUSTOM_STORE_HOUSE, MoreSourceBean::class.java)
        custom.removeFirstIf {
            it.sourceUrl == deleteData.sourceUrl
        }
        if (deleteData.isServer) {
            KVStorage.putBoolean(deleteData.sourceUrl, true)
        }
        mAdapter.remove(position)
    }

    private fun selectItem(position: Int) {
        val selectData = mAdapter.data[position]
        mLastSelectBean?.let {
            it.isSelected = false
            val index = mAdapter.data.indexOf(it)
            mAdapter.notifyItemChanged(index)
        }
        selectData.let {
            it.isSelected = true
            mAdapter.notifyItemChanged(position)
            mRecyclerView?.setSelectedPosition(position)
        }
        mLastSelectBean = selectData
        KVStorage.putBean(HawkConfig.CUSTOM_STORE_HOUSE_SELECTED, selectData)
        this@SourceStoreDialog.dismiss()
        Toast.makeText(context, "稍等片刻，正在打开线路切换弹框", Toast.LENGTH_SHORT).show()
        SourceLineDialogUtil(activity).getData {
            if (activity is SettingActivity) {
                activity.onBackPressed()
            }
            if (activity is HomeActivity) {
                activity.forceRestartHomeActivity()
            }

        }
    }

    class MoreSourceAdapter :
        BaseQuickAdapter<MoreSourceBean, BaseViewHolder>(R.layout.item_dialog_api_history) {

        override fun createBaseViewHolder(view: View?): BaseViewHolder {
            val holder = super.createBaseViewHolder(view)
            holder.addOnClickListener(R.id.tvDel)
            holder.setVisible(R.id.tvDel, true)
            holder.addOnClickListener(R.id.tvName)
            return holder
        }

        override fun convert(holder: BaseViewHolder, item: MoreSourceBean) {
            showDefault(item, holder)
            if (item.isSelected) {
                val text = holder.getView<TextView>(R.id.tvName).text
                holder.setText(
                    R.id.tvName,
                    SpanUtils.with(holder.getView(R.id.tvName)).appendImage(
                        ContextCompat.getDrawable(
                            holder.getView<TextView>(R.id.tvName).context,
                            R.drawable.ic_select_fill
                        )!!
                    ).append(" ").append(text).create()
                )
            } else {
                showDefault(item, holder)
            }
        }

        private fun showDefault(
            item: MoreSourceBean?,
            helper: BaseViewHolder?
        ) {
            if (!item?.sourceName.isNullOrEmpty()) {
                helper?.setText(R.id.tvName, item?.sourceName)
            } else if (!item?.sourceUrl.isNullOrEmpty()) {
                helper?.setText(R.id.tvName, item?.sourceUrl)
            }
        }


    }

    private fun refeshQRcode() {
        val address = ControlManager.get().getAddress(false)
        mQrCode?.setImageBitmap(
            QRCodeGen.generateBitmap(
                address,
                AutoSizeUtils.mm2px(context, 200f),
                AutoSizeUtils.mm2px(context, 200f)
            )
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleRemotePush(refreshEvent: RefreshEvent) {
        when (refreshEvent.type) {
            RefreshEvent.TYPE_STORE_PUSH -> {
                val moreSourceBean = refreshEvent.obj as MoreSourceBean
                DEFAULT_STORE_URL = moreSourceBean.sourceUrl
                getMutiSource(moreSourceBean)
            }
        }

    }

    private fun checkUrlIsValid(url: String?): Boolean {
        return !url.isNullOrEmpty() && (url.startsWith("clan://")
                || url.startsWith("https://") || url.startsWith("http://"))
    }

}