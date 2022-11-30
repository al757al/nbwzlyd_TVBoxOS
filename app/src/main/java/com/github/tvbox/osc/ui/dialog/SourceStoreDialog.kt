package com.github.tvbox.osc.ui.dialog

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.UA
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
import com.github.tvbox.osc.util.ScreenUtils
import com.github.tvbox.osc.util.StringUtils
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

    private val userAgent = "https://gitcode"
    private val prefix = "clan://"
    private val address by lazy {
        ControlManager.get().getAddress(false)
    }
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
        if (mAdapter.data.isNotEmpty()) {
            Hawk.put(HawkConfig.CUSTOM_STORE_HOUSE_DATA, mAdapter.data)
        }
        super.dismiss()
    }

    //    private var DEFAULT_STORE_URL = "https://gitcode.net/wzlyd1/00/-/raw/master/000.txt"
    private var DEFAULT_STORE_URL = ""

    private val DEFAULT_DATA = LinkedHashMap<String, MoreSourceBean>()

    init {
        setContentView(R.layout.more_source_dialog_select)
        mRecyclerView = findViewById(R.id.list)
        mAddMoreBtn = findViewById(R.id.inputSubmit)
        mSourceNameEdit = findViewById(R.id.input_sourceName)
        mSourceUrlEdit = findViewById(R.id.input_source_url)
        mAddMoreBtn = findViewById(R.id.inputSubmit)
        mQrCode = findViewById(R.id.qrCode)
        findViewById<View>(R.id.jump_web).setOnClickListener {
            val intent = Intent()
            // 设置意图动作为打开浏览器
            intent.action = Intent.ACTION_VIEW
            // 声明一个Uri
            val uri: Uri = Uri.parse(address)
            intent.data = uri
            context.startActivity(intent)
        }
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
                    AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("确定要删除?").setPositiveButton(
                            "确定"
                        ) { dialog, which -> deleteItem(position) }.setNegativeButton("点错了", null)
                        .create().show()
                }
                R.id.tvName -> {
                    selectItem(position)

                }
                R.id.tvCopy -> {
                    val item = mAdapter.getItem(position)
                    val text = "${item?.sourceName}\n${item?.sourceUrl}"
                    StringUtils.copyText(context, text)
                }
            }
        }
        refeshQRcode()
        inflateCustomSource(mutableListOf())
    }

    private fun saveCustomSourceBean(sourceUrl0: String?, sourceName0: String?) {
        if (!checkUrlIsValid(sourceUrl0)) {
            return
        }
        var lineSource = sourceUrl0
        if (lineSource?.startsWith(prefix) == true) {
            lineSource = ApiConfig.clanToAddress(lineSource)
        }
        val saveList =
            Hawk.get(HawkConfig.CUSTOM_STORE_HOUSE_DATA, ArrayList<MoreSourceBean>())
        val sourceBean = MoreSourceBean().apply {
            this.sourceUrl = lineSource.toString()
            this.sourceName = sourceName0?.ifEmpty { "自用仓库" + saveList.size }.toString()
            this.isServer = false
        }
        if (!saveList.contains(sourceBean)) {
            mAdapter.addData(sourceBean)
            mRecyclerView?.scrollToPosition(0)
            saveList.add(sourceBean)
            Hawk.put(HawkConfig.CUSTOM_STORE_HOUSE_DATA, saveList)
        }
        mSourceUrlEdit?.setText("")
        mSourceNameEdit?.setText("")
    }


    private fun getMutiSource(moreSourceBean: MoreSourceBean? = null) {
        mLoading.letVisible()
        if (moreSourceBean?.sourceUrl?.startsWith(prefix) == true) {
            moreSourceBean.sourceUrl = ApiConfig.clanToAddress(moreSourceBean.sourceUrl)
        }
        val req = OkGo.get<String>(moreSourceBean?.sourceUrl)
            .cacheMode(CacheMode.IF_NONE_CACHE_REQUEST)
        if (moreSourceBean?.sourceUrl?.startsWith(userAgent) == true) {
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
                //加载本地存储的
                inflateCustomSource(mutableListOf())
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
            var tempKey: String? = null
            if (moreSourceBean?.sourceUrl?.contains(";pk;") == true) {
                tempKey = moreSourceBean.sourceUrl.split(";pk;")[1]
            }
            val findResult = ApiConfig.FindResult(response?.body(), tempKey)
            val jsonObj = JSONObject(findResult ?: return)
            var jsonArray: JSONArray? = null
            if (!jsonObj.has("storeHouse")) {
                if (jsonObj.has("urls")) {//可能是单仓库
                    saveCustomSourceBean(
                        moreSourceBean?.sourceUrl ?: "",
                        moreSourceBean?.sourceName ?: ""
                    )
                } else if (jsonObj.has("sites")) {//可能是线路
                    Hawk.put(HawkConfig.API_URL, moreSourceBean?.sourceUrl)
                    val historySourceBeanList =
                        Hawk.get(HawkConfig.API_HISTORY_LIST, ArrayList<MoreSourceBean>())
                    moreSourceBean?.let {
                        if (historySourceBeanList.indexOf(moreSourceBean) == -1) {
                            if (moreSourceBean.sourceName.isEmpty()) {
                                moreSourceBean.sourceName = "自定义配置线路${historySourceBeanList.size}"
                            }
                            moreSourceBean.isServer = false
                            historySourceBeanList.add(it)
                        }
                        Hawk.put(HawkConfig.API_HISTORY_LIST, historySourceBeanList)
                    }
//                    if (history.size > 20) history.removeAt(20)
//                    Hawk.put(HawkConfig.API_HISTORY, history)
                    ToastUtils.showShort("系统识别到你推送的可能是线路，已经帮你保存并重启首页")

                    JumpUtils.forceRestartHomeActivity(context)
                    this.dismiss()
                } else {//无法识别了
                    val text =
                        SpanUtils().append("你的仓库格式不对\n请参考公众号").append(" <仓库定义规则> ")
                            .setBold()
                            .setForegroundColor(Color.RED).append("文章").create()
                    ToastUtils.showShort(text)
                    return
                }
            } else {
                jsonArray = jsonObj.getJSONArray("storeHouse")
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
                val result = DEFAULT_DATA.map {
                    it.value
                }.toMutableList()

                inflateCustomSource(result)
            }

        } catch (e: Exception) {
            Toast.makeText(context, "JSON解析失败${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun inflateCustomSource(serverResult: MutableList<MoreSourceBean>) {
        val localData =
            Hawk.get(HawkConfig.CUSTOM_STORE_HOUSE_DATA, ArrayList<MoreSourceBean>())
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
            Hawk.get(
                HawkConfig.CUSTOM_STORE_HOUSE_SELECTED,
                MoreSourceBean()
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
        Hawk.put(HawkConfig.CUSTOM_STORE_HOUSE_DATA, localData)

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
        val custom =
            Hawk.get(HawkConfig.CUSTOM_STORE_HOUSE_DATA, ArrayList<MoreSourceBean>())
        custom.removeFirstIf {
            it.sourceUrl == deleteData.sourceUrl
        }
        mAdapter.remove(position)
        Hawk.put(HawkConfig.CUSTOM_STORE_HOUSE_DATA, custom)
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
        Hawk.put(HawkConfig.CUSTOM_STORE_HOUSE_SELECTED, selectData)
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
            holder.addOnClickListener(R.id.tvCopy)
            holder.setVisible(R.id.tvDel, true)
            holder.setGone(R.id.tvCopy, !ScreenUtils.isTv(view?.context))
            holder.addOnClickListener(R.id.tvName)
            return holder
        }

        override fun convert(holder: BaseViewHolder, item: MoreSourceBean) {
            showDefault(item, holder)
            val textView = holder.getView<TextView>(R.id.tvName)
            if (item.isSelected) {
                holder.setText(
                    R.id.tvName,
                    SpanUtils.with(holder.getView(R.id.tvName)).appendImage(
                        ContextCompat.getDrawable(
                            holder.getView<TextView>(R.id.tvName).context,
                            R.drawable.ic_select_fill
                        )!!
                    ).append(" ").append(textView.text).create()
                )
//                textView.requestFocus()
            } else {
//                textView.clearFocus()
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
                getMutiSource(moreSourceBean)
            }
        }

    }

    private fun checkUrlIsValid(url: String?): Boolean {
        return !url.isNullOrEmpty() && (url.startsWith("clan://")
                || url.startsWith("https://") || url.startsWith("http://"))
    }

}