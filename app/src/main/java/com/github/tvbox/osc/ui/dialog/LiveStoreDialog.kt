package com.github.tvbox.osc.ui.dialog

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.tvbox.osc.R
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.bean.LiveSourceBean
import com.github.tvbox.osc.event.RefreshEvent
import com.github.tvbox.osc.ext.removeFirstIf
import com.github.tvbox.osc.server.ControlManager
import com.github.tvbox.osc.ui.dialog.util.AdapterDiffCallBack
import com.github.tvbox.osc.ui.dialog.util.MyItemTouchHelper
import com.github.tvbox.osc.ui.tv.QRCodeGen
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.KVStorage
import com.github.tvbox.osc.util.urlhttp.JumpUtils
import com.owen.tvrecyclerview.widget.TvRecyclerView
import me.jessyan.autosize.utils.AutoSizeUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

//直播多源地址
class LiveStoreDialog(private val activity: Activity) : BaseDialog(activity) {
    private var mRecyclerView: TvRecyclerView? = null
    private var mAddMoreBtn: TextView? = null
    private var mLastSelectBean: LiveSourceBean? = null
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
        KVStorage.putList(HawkConfig.LIVE_SOURCE_URL_HISTORY, mAdapter.data)
        super.dismiss()
    }

    init {
        setContentView(R.layout.live_source_dialog_select)
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
                Toast.makeText(this@LiveStoreDialog.context, "请输入直播源地址！", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            handleRemotePush(RefreshEvent(RefreshEvent.TYPE_STORE_PUSH).apply {
                this.obj = LiveSourceBean().apply {
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
                R.id.tvName -> {//重启liveactivity
                    val liveSourceBean = mAdapter.data[position]
                    if (liveSourceBean.isOfficial) {
                        KVStorage.putBoolean(HawkConfig.USE_CUSTOM_LIVE_URL, false)
                        JumpUtils.forceRestartHomeActivity(context)
                        return@setOnItemChildClickListener
                    }
//                    https://agit.ai/yan11xx/TVBOX/raw/branch/master/live/tv.txt
                    KVStorage.putBoolean(HawkConfig.USE_CUSTOM_LIVE_URL, true)
                    selectNewLiveSource(mAdapter.data[position])

                }
            }
        }
        refeshQRcode()
        val list = KVStorage.getList(
            HawkConfig.LIVE_SOURCE_URL_HISTORY,
            LiveSourceBean::class.java
        )
        inflateCustomSource(list)
    }

    private fun selectNewLiveSource(liveSourceBean: LiveSourceBean) {
        KVStorage.putBean(HawkConfig.LIVE_SOURCE_URL_CURRENT, liveSourceBean)
        this.dismiss()
        ApiConfig.get().loadLiveSourceUrl(null, null)
        val intent = Intent(context, com.github.tvbox.osc.ui.activity.LivePlayActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        ActivityUtils.startActivity(intent)
    }

    private fun saveCustomSourceBean(liveSourceBean: LiveSourceBean) {
        val sourceUrl0 = liveSourceBean.sourceUrl
        val sourceName0 = liveSourceBean.sourceName
        if (sourceUrl0.startsWith("http") || sourceUrl0.startsWith("https")) {
            val saveList =
                KVStorage.getList(HawkConfig.LIVE_SOURCE_URL_HISTORY, LiveSourceBean::class.java)
            if (saveList.contains(liveSourceBean)) {
                return
            }
            val sourceBean = LiveSourceBean().apply {
                this.sourceUrl = sourceUrl0
                this.sourceName = sourceName0.ifEmpty { "自用直播源" + saveList.size }
            }
            mAdapter.addData(sourceBean)
            mRecyclerView?.scrollToPosition(0)
            saveList.add(sourceBean)
            mSourceUrlEdit?.setText("")
            mSourceNameEdit?.setText("")
        } else {
            Toast.makeText(this@LiveStoreDialog.context, "请输入直播源地址！", Toast.LENGTH_LONG)
                .show()
        }
    }


    private fun inflateCustomSource(result: MutableList<LiveSourceBean>) {
        val localData =
            KVStorage.getList(HawkConfig.LIVE_SOURCE_URL_HISTORY, LiveSourceBean::class.java)
        if (localData.isEmpty() && result.isNotEmpty()) {//如果本地保存的是空的，就把新的结果放进去
            localData.addAll(result)
        } else {//否则进行匹配，只保存本地没有的
            val customMap = localData.associateBy { it.uniKey }
            val newResultMap = result.associateBy { it.uniKey }
            newResultMap.forEach {
                if (customMap[it.key] == null) {
                    localData.add(it.value)
                }
            }
        }
        val useCustomLiveSource = KVStorage.getBoolean(HawkConfig.USE_CUSTOM_LIVE_URL, false)
        var index = 0
        if (useCustomLiveSource) {
            val lastSelectBean =
                KVStorage.getBean(
                    HawkConfig.LIVE_SOURCE_URL_CURRENT,
                    LiveSourceBean::class.java
                )
            localData.forEach {
                if (it.sourceUrl != lastSelectBean?.sourceUrl) {
                    it.isSelected = false
                } else {
                    it.isSelected = true
                    index = result.indexOf(it)
                }
            }
        } else {
            localData.forEach {
                it.isSelected = false
            }
        }
        val data = LiveSourceBean().apply {
            sourceName = "点击重启，恢复默认直播"
            sourceUrl = ""
            isOfficial = true
        }
        if (!localData.contains(data)) {
            localData.add(0, data)
        }
        val diffResult =
            DiffUtil.calculateDiff(AdapterDiffCallBack(mAdapter.data, localData), false)
        //为了适配diffUtil才这么写的
        mAdapter.data.clear()
        mAdapter.data.addAll(localData)
        //更新最新的地址
        diffResult.dispatchUpdatesTo(mAdapter)
        if (index != -1) {
            mRecyclerView?.post {
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
            KVStorage.getList(HawkConfig.LIVE_SOURCE_URL_HISTORY, LiveSourceBean::class.java)
        custom.removeFirstIf {
            it.sourceUrl == deleteData.sourceUrl
        }
        val currentBean =
            KVStorage.getBean(HawkConfig.LIVE_SOURCE_URL_CURRENT, LiveSourceBean::class.java)
        if (deleteData.uniKey == currentBean?.uniKey) {
            KVStorage.remove(HawkConfig.LIVE_SOURCE_URL_CURRENT)
        }
        KVStorage.putList(HawkConfig.LIVE_SOURCE_URL_HISTORY, custom)
        mAdapter.remove(position)
    }

    class MoreSourceAdapter :
        BaseQuickAdapter<LiveSourceBean, BaseViewHolder>(R.layout.item_dialog_api_history) {

        override fun createBaseViewHolder(view: View?): BaseViewHolder {
            val holder = super.createBaseViewHolder(view)
            holder.addOnClickListener(R.id.tvDel)
            holder.addOnClickListener(R.id.tvName)
            return holder
        }

        override fun convert(holder: BaseViewHolder, item: LiveSourceBean) {
            showDefault(item, holder)
            holder.setVisible(R.id.tvDel, !item.isOfficial)

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
            item: LiveSourceBean?,
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

    @Subscribe
    fun handleRemotePush(refreshEvent: RefreshEvent) {
        when (refreshEvent.type) {
            RefreshEvent.TYPE_STORE_PUSH -> {
                val moreSourceBean = refreshEvent.obj as LiveSourceBean
                saveCustomSourceBean(moreSourceBean)
            }
        }

    }

}