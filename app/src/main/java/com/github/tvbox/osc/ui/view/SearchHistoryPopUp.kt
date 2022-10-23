package com.github.tvbox.osc.ui.view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import com.github.tvbox.osc.R
import com.github.tvbox.osc.event.ServerEvent
import com.github.tvbox.osc.event.ServerEvent.SERVER_SEARCH
import com.github.tvbox.osc.ui.adapter.SearchHistoryAdapter
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.KVStorage
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.owen.tvrecyclerview.widget.TvRecyclerView
import org.greenrobot.eventbus.EventBus
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AnimationHelper
import razerdp.util.animation.TranslationConfig


/**
 * <pre>
 *     author : derek
 *     time   : 2022/10/18
 *     desc   :
 *     version:
 * </pre>
 */
class SearchHistoryPopUp(context: Context) : BasePopupWindow(context) {
    private var historyAdapter: SearchHistoryAdapter? = null
    private var mGridView: TvRecyclerView? = null

    init {
        setContentView(R.layout.search_history_layout)
        mGridView = contentView.findViewById(R.id.mGridView)
        mGridView = findViewById(R.id.mGridView)
        historyAdapter = SearchHistoryAdapter()
        mGridView?.run {
            setHasFixedSize(true)
            layoutManager = FlexboxLayoutManager(getContext(), FlexDirection.ROW, FlexWrap.WRAP)
            adapter = historyAdapter
            isFocusableInTouchMode = true
            requestFocus()
        }
        setBackgroundColor(Color.TRANSPARENT)
        historyAdapter?.setOnItemClickListener { adapter, view, position ->
            EventBus.getDefault().post(ServerEvent(SERVER_SEARCH,historyAdapter?.getItem(position)))
            dismiss()
        }
    }

    override fun onWindowFocusChanged(popupDecorViewProxy: View?, hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(popupDecorViewProxy, hasWindowFocus)
        mGridView?.isFocusable = true
        mGridView?.requestFocus()
    }


    override fun onCreateShowAnimation(): Animation {
        return AnimationHelper.asAnimation().withTranslation(TranslationConfig.FROM_LEFT).toShow()

    }

    override fun onCreateDismissAnimation(): Animation {
        return AnimationHelper.asAnimation().withTranslation(TranslationConfig.TO_LEFT)
            .toDismiss()
    }

    override fun showPopupWindow() {
        super.showPopupWindow()
        popupWindow.isFocusable = true
        (context as? Activity)?.window?.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        historyAdapter?.setNewData(KVStorage.getList(HawkConfig.SEARCH_HISTORY, String::class.java))
    }

    override fun onDismiss() {
        (context as? Activity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        super.onDismiss()
    }
}