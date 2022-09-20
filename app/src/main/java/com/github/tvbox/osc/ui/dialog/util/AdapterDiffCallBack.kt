package com.github.tvbox.osc.ui.dialog.util

import com.github.tvbox.osc.bean.MoreSourceBean
import androidx.recyclerview.widget.DiffUtil
import android.text.TextUtils
import com.github.tvbox.osc.bean.BaseItem

/**
 * <pre>
 * author : derek
 * time   : 2022/09/20
 * desc   :
 * version:
</pre> *
 */
class AdapterDiffCallBack  internal constructor(
    private val oldItems: MutableList<out BaseItem>,
    private val newItems: MutableList<out BaseItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldItems.size
    }

    override fun getNewListSize(): Int {
        return newItems.size
    }

    // 判断Item是否已经存在
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return TextUtils.equals(oldItem.uniKey,newItem.uniKey)
    }

    // 如果Item已经存在则会调用此方法，判断Item的内容是否一致
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return TextUtils.equals(oldItem.uniKey,newItem.uniKey)
    }
}