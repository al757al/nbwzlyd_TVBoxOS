package com.github.tvbox.osc.ui.view

import android.content.Context
import android.graphics.Color
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.github.tvbox.osc.R
import com.github.tvbox.osc.base.App
import com.github.tvbox.osc.bean.VodInfo
import com.github.tvbox.osc.cache.RoomDataManger
import com.github.tvbox.osc.event.RefreshEvent
import com.github.tvbox.osc.ui.activity.PlayEvent
import com.github.tvbox.osc.ui.adapter.SeriesAdapter
import com.github.tvbox.osc.util.FastClickCheckUtil
import com.owen.tvrecyclerview.widget.TvRecyclerView
import com.owen.tvrecyclerview.widget.V7GridLayoutManager
import org.greenrobot.eventbus.EventBus
import razerdp.basepopup.BasePopupWindow

class ChoosePlayPopUp(context: Context?) : BasePopupWindow(context) {
    private var seriesAdapter: SeriesAdapter? = null
    private var mGridView: TvRecyclerView? = null

    init {
        setContentView(R.layout.choose_play_layout)
        mGridView = contentView.findViewById(R.id.mGridView)
        mGridView = findViewById(R.id.mGridView)
        seriesAdapter = SeriesAdapter()
        mGridView?.run {
            setHasFixedSize(true)
            layoutManager = V7GridLayoutManager(getContext(), 6)
            adapter = seriesAdapter
        }
        seriesAdapter?.onItemClickListener = object : BaseQuickAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
                FastClickCheckUtil.check(view)
                val vodInfo = App.instance?.getVodInfo() ?: return
                if ((vodInfo.seriesMap.get(vodInfo.playFlag)?.size ?: 0) > 0) {
                    for (j in vodInfo.seriesMap.get(vodInfo.playFlag)?.indices!!) {
                        seriesAdapter!!.data[j].selected = false
                        seriesAdapter!!.notifyItemChanged(j)
                    }
                    //解决倒叙不刷新
                    if (vodInfo.playIndex != position) {
                        seriesAdapter!!.data[position].selected = true
                        seriesAdapter!!.notifyItemChanged(position)
                        vodInfo.playIndex = position
                    }
                    seriesAdapter!!.data[vodInfo.playIndex].selected = true
                    seriesAdapter!!.notifyItemChanged(vodInfo.playIndex)
                    insertVod(vodInfo)
                    EventBus.getDefault().post(PlayEvent().apply {
                        this.vodInfo = vodInfo
                    })
                }
            }

        }
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun showPopupWindow(anchorView: View?) {
        super.showPopupWindow(anchorView)
        popupWindow.isFocusable = true
        seriesAdapter?.setNewData(App.instance?.getVodInfo()?.seriesMap?.get(App.instance?.getVodInfo()?.playFlag))
    }

    private fun insertVod(vodInfo: VodInfo) {
        try {
            vodInfo.playNote = vodInfo.seriesMap[vodInfo.playFlag]!![vodInfo.playIndex].name
        } catch (th: Throwable) {
            vodInfo.playNote = ""
        }
        RoomDataManger.insertVodRecord(vodInfo.sourceKey, vodInfo)
        EventBus.getDefault().post(RefreshEvent(RefreshEvent.TYPE_HISTORY_REFRESH))
    }

}