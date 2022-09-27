package com.github.tvbox.osc.ui.dialog.util

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.github.tvbox.osc.bean.BaseItem
import java.util.*

/**
 * <pre>
 * author : derek
 * time   : 2022/09/26
 * desc   :
 * version:
</pre> *
 */
class MyItemTouchHelper(
    private val baseBeans: MutableList<out BaseItem?>,
    private val recycleViewAdapter: RecyclerView.Adapter<*>
) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        val dragFlags =
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or  ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        recyclerView.parent.requestDisallowInterceptTouchEvent(true)
        //得到当拖拽的viewHolder的Position
        val fromPosition = viewHolder.adapterPosition
        //拿到当前拖拽到的item的viewHolder
        val toPosition = target.adapterPosition
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(baseBeans, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(baseBeans, i, i - 1)
            }
        }
        recycleViewAdapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }


    /**
     * 重写拖拽不可用
     *
     * @return
     */
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {

    }
}