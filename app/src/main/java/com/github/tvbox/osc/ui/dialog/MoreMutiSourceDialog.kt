package com.github.tvbox.osc.ui.dialog

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.tvbox.osc.R
import com.github.tvbox.osc.bean.MoreSourceBean
import com.github.tvbox.osc.ext.findFirst
import com.github.tvbox.osc.ext.removeFirstIf
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.KVStorage
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.owen.tvrecyclerview.widget.TvRecyclerView
import org.jsoup.Jsoup
import java.util.regex.Pattern

//多源地址
class MoreMutiSourceDialog(context: Context) : BaseDialog(context) {
    private var mRecyclerView: TvRecyclerView? = null
    private var mAddMoreBtn: TextView? = null
    private var mLastSelectBean: MoreSourceBean? = null
    private var mSourceNameEdit: EditText? = null
    private var mSourceUrlEdit: EditText? = null
    private val mAdapter: MoreSourceAdapter by lazy {
        MoreSourceAdapter()
    }

    companion object {
        private val DEFAULT_URLS = mutableListOf<MoreSourceBean>(

        )
    }

    init {
        setContentView(R.layout.more_source_dialog_select)
        mRecyclerView = findViewById(R.id.list)
        mAddMoreBtn = findViewById(R.id.inputSubmit)
        mSourceNameEdit = findViewById(R.id.input_sourceName)
        mSourceUrlEdit = findViewById(R.id.input_source_url)
        mAddMoreBtn = findViewById(R.id.inputSubmit)
        mRecyclerView?.adapter = mAdapter
        mAddMoreBtn?.setOnClickListener {
            val sourceUrl0 = mSourceUrlEdit?.text.toString()
            val sourceName0 = mSourceNameEdit?.text.toString()
            if (sourceUrl0.isEmpty()) {
                Toast.makeText(this@MoreMutiSourceDialog.context, "请输入源地址！", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            if (sourceUrl0.startsWith("http") || sourceUrl0.startsWith("https") || sourceUrl0.startsWith(
                    "clan"
                )
            ) {
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
            } else {
                Toast.makeText(this@MoreMutiSourceDialog.context, "请输入正确的源地址！", Toast.LENGTH_LONG)
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
        getMutiSource()
    }


    private fun getMutiSource() {
        OkGo.get<String>("http://5.nxog.top/m/dc/").cacheTime(3 * 24 * 60 * 60 * 1000)
            .cacheMode(CacheMode.IF_NONE_CACHE_REQUEST)
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    jsoupInflateData(response)
                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                }

                override fun onCacheSuccess(response: Response<String>?) {
                    super.onCacheSuccess(response)
                    jsoupInflateData(response)

                }

            })
    }

    private fun jsoupInflateData(response: Response<String>?) {
        val jsoup = Jsoup.parse(response?.body())
        val element = jsoup.body().getElementsByClass("col-sm-4")
        val data = mutableListOf<MoreSourceBean>()
        element.forEach {
            if (it.getElementsByClass("h4 push-5").size != 0) {
                val moreSourceBean = MoreSourceBean().apply {
                    sourceName = it.getElementsByClass("h4 push-5").text()
                    val pattern =
                        Pattern.compile("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?")
                    val matcher =
                        pattern.matcher(it.getElementsByClass("ribbon-success").attr("href"))
                    if (matcher.find()) {
                        sourceUrl = matcher.group(0) ?: ""
                    }
                    Log.d("derek110", sourceName + "\n" + sourceUrl)
                }
                data.add(moreSourceBean)
            }
        }
        val custom = KVStorage.getList(HawkConfig.CUSTOM_STORE_HOUSE, MoreSourceBean::class.java)
        if (custom.isNotEmpty()) {
            data.addAll(0, custom)
        }
        val lastSelectBean =
            KVStorage.getBean(HawkConfig.CUSTOM_STORE_HOUSE_SELECTED, MoreSourceBean::class.java)
        data.findFirst {
            it.sourceUrl == lastSelectBean?.sourceUrl
        }?.let {
            it.isSelected = true
            mAdapter.setNewData(data)
            mRecyclerView?.scrollToPosition(mAdapter.data.indexOf(it))
        }
    }

    private fun deleteItem(position: Int) {
        val deleteData = mAdapter.data[position]
        val custom = KVStorage.getList(HawkConfig.CUSTOM_STORE_HOUSE, MoreSourceBean::class.java)
        custom.removeFirstIf {
            it.sourceUrl == deleteData.sourceUrl
        }
        KVStorage.putList(HawkConfig.CUSTOM_STORE_HOUSE, custom)
        mAdapter.remove(position)
    }

    private fun selectItem(position: Int) {
        val selectData = mAdapter.data[position]
        var selectedUrl = ""
        var selectName = ""
        mLastSelectBean?.let {
            it.isSelected = false
            val index = mAdapter.data.indexOf(it)
            mAdapter.notifyItemChanged(index)
        }
        selectData.let {
            it.isSelected = true
            selectedUrl = it.sourceUrl
            selectName = it.sourceName
            mAdapter.notifyItemChanged(position)
            mRecyclerView?.setSelectedPosition(position)
        }
        mLastSelectBean = selectData
        KVStorage.putBean(HawkConfig.CUSTOM_STORE_HOUSE_SELECTED, selectData)
        this@MoreMutiSourceDialog.dismiss()
    }

    class MoreSourceAdapter :
        BaseQuickAdapter<MoreSourceBean, BaseViewHolder>(R.layout.item_dialog_api_history) {

        override fun createBaseViewHolder(view: View?): BaseViewHolder {
            val holder = super.createBaseViewHolder(view)
            holder.addOnClickListener(R.id.tvDel)
            holder.addOnClickListener(R.id.tvName)
            return holder
        }

        override fun convert(holder: BaseViewHolder, item: MoreSourceBean) {
            showDefault(item, holder)
            holder.setGone(R.id.tvDel, !item.isServer && !item.isSelected)
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


}