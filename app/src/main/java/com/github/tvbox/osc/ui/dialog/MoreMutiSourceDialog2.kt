package com.github.tvbox.osc.ui.dialog

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.tvbox.osc.R
import com.github.tvbox.osc.bean.MoreSourceBean
import com.github.tvbox.osc.event.RefreshEvent
import com.github.tvbox.osc.ext.removeFirstIf
import com.github.tvbox.osc.server.ControlManager
import com.github.tvbox.osc.ui.tv.QRCodeGen
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.KVStorage
import com.owen.tvrecyclerview.widget.TvRecyclerView
import me.jessyan.autosize.utils.AutoSizeUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

//多源地址
class MoreMutiSourceDialog2(context: Context) : BaseDialog(context) {
    private var mRecyclerView: TvRecyclerView? = null
    private var mAddMoreBtn: TextView? = null
    private var mLastSelectBean: MoreSourceBean? = null
    private var mSourceNameEdit: EditText? = null
    private var mSourceUrlEdit: EditText? = null
    private var mQrCode: ImageView? = null
    private val mAdapter: MoreSourceAdapter by lazy {
        MoreSourceAdapter()
    }

    override fun show() {
        EventBus.getDefault().register(this)
        super.show()
    }

    override fun dismiss() {
        EventBus.getDefault().unregister(this)
        super.dismiss()
    }

    private val DEFAULT_DATA = mutableListOf(
            MoreSourceBean().apply {
                this.sourceName = "白嫖仓库"
                this.sourceUrl =
                        "https://gitea.com/33/3/raw/branch/3/3/3/tv/update_yuan"
            },
            MoreSourceBean().apply {
                this.sourceName = "ygfxz仓库"
                this.sourceUrl =
                        "https://gitea.com/ygfxz/apk_release/raw/branch/main/tv/update_yuan"
            },
            MoreSourceBean().apply {
                this.sourceName = "syzxasdc仓库"
                this.sourceUrl =
                        "https://gitea.com/syzxasdc/apk_release1/raw/branch/main/tv/update_yuan"
            },
            MoreSourceBean().apply {
                this.sourceName = "apkcore仓库"
                this.sourceUrl =
                        "https://gitea.com/apkcore/apk_release/raw/branch/main/tv/update_yuan"
                this.isServer = true
            },
            MoreSourceBean().apply {
                this.sourceName = "ye仓库"
                this.sourceUrl =
                        "https://gitea.com/ye/apk_release/raw/branch/main/tv/update_yuan"
                this.isServer = true
            },
            MoreSourceBean().apply {
                this.sourceName = "xnpc仓库"
                this.sourceUrl =
                        "https://gitea.com/xnpc/apk_release/raw/branch/main/tv/update_yuan"
                this.isServer = true
            },
            MoreSourceBean().apply {
                this.sourceName = "manthow仓库"
                this.sourceUrl =
                        "https://gitea.com/manthow/apk_release/raw/branch/main/tv/update_yuan"
                this.isServer = true
            },
            MoreSourceBean().apply {
                this.sourceName = "thorjsbox仓库"
                this.sourceUrl =
                        "https://gitea.com/thorjsbox/apk_release/raw/branch/main/tv/update_yuan"
                this.isServer = true
            },
            MoreSourceBean().apply {
                this.sourceName = "zhanghong仓库"
                this.sourceUrl =
                        "https://gitea.com/zhanghong/apk_release/raw/branch/main/tv/update_yuan"
                this.isServer = true
            },
            MoreSourceBean().apply {
                this.sourceName = "bo仓库"
                this.sourceUrl =
                        "https://gitea.com/bo/apk_release/raw/branch/main/tv/update_yuan"
                this.isServer = true
            },
    )

    init {
        setContentView(R.layout.more_source_dialog_select)
        mRecyclerView = findViewById(R.id.list)
        mAddMoreBtn = findViewById(R.id.inputSubmit)
        mSourceNameEdit = findViewById(R.id.input_sourceName)
        mSourceUrlEdit = findViewById(R.id.input_source_url)
        mAddMoreBtn = findViewById(R.id.inputSubmit)
        mQrCode = findViewById(R.id.qrCode)
        mRecyclerView?.adapter = mAdapter
        mAddMoreBtn?.setOnClickListener {
            val sourceUrl0 = mSourceUrlEdit?.text.toString()
            val sourceName0 = mSourceNameEdit?.text.toString()
            if (sourceUrl0.isEmpty()) {
                Toast.makeText(this@MoreMutiSourceDialog2.context, "请输入仓库地址！", Toast.LENGTH_LONG)
                        .show()
                return@setOnClickListener
            }
            if (sourceUrl0.startsWith("http") || sourceUrl0.startsWith("https")) {
                val saveList =
                        KVStorage.getList(HawkConfig.CUSTOM_STORE_HOUSE, MoreSourceBean::class.java)
                val sourceBean = MoreSourceBean().apply {
                    this.sourceUrl = sourceUrl0
                    this.sourceName = sourceName0.ifEmpty { "自用仓库" + saveList.size }
                    this.isServer = false
                }
                mAdapter.addData(0, sourceBean)
                mRecyclerView?.scrollToPosition(0)
                saveList.add(sourceBean)
                KVStorage.putList(HawkConfig.CUSTOM_STORE_HOUSE, saveList)
                mSourceUrlEdit?.setText("")
                mSourceNameEdit?.setText("")
            } else {
                Toast.makeText(this@MoreMutiSourceDialog2.context, "请输入仓库地址！", Toast.LENGTH_LONG)
                        .show()
            }

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
        getMutiSource()
    }


    private fun getMutiSource() {
        val result = DEFAULT_DATA.filter {
            !KVStorage.getBoolean(it.sourceUrl, false)
        }.toMutableList()

        val custom = KVStorage.getList(HawkConfig.CUSTOM_STORE_HOUSE, MoreSourceBean::class.java)
        if (custom.isNotEmpty()) {
            result.addAll(0, custom)
        }
        val lastSelectBean =
                KVStorage.getBean(HawkConfig.CUSTOM_STORE_HOUSE_SELECTED, MoreSourceBean::class.java)
        var index = 0
        result.forEach {
            if (it.sourceUrl != lastSelectBean?.sourceUrl) {
                it.isSelected = false
            } else {
                it.isSelected = true
                index = result.indexOf(it)
            }
        }
        mAdapter.setNewData(result)
        mRecyclerView?.post {
            mRecyclerView?.scrollToPosition(index)
        }
    }

    //删除仓库地址
    private fun deleteItem(position: Int) {
        val deleteData = mAdapter.data[position]
        val custom = KVStorage.getList(HawkConfig.CUSTOM_STORE_HOUSE, MoreSourceBean::class.java)
        custom.removeFirstIf {
            it.sourceUrl == deleteData.sourceUrl
        }
        KVStorage.putList(HawkConfig.CUSTOM_STORE_HOUSE, custom)
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
        this@MoreMutiSourceDialog2.dismiss()
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
                var text = holder.getView<TextView>(R.id.tvName).text
                text = "√ $text"
                holder.setText(R.id.tvName, text)
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
        mQrCode?.setImageBitmap(QRCodeGen.generateBitmap(address,
                AutoSizeUtils.mm2px(context, 200f),
                AutoSizeUtils.mm2px(context, 200f)))
    }

    @Subscribe
    fun handleRemotePush(refreshEvent: RefreshEvent) {
        when (refreshEvent.type) {
            RefreshEvent.TYPE_STORE_PUSH -> {
                val moreSourceBean = refreshEvent.obj as MoreSourceBean
                mSourceNameEdit?.setText(moreSourceBean.sourceName)
                mSourceUrlEdit?.setText(moreSourceBean.sourceUrl)
            }
        }

    }

}