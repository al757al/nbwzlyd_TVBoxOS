package com.github.tvbox.osc.ui.dialog

import android.app.Activity
import androidx.recyclerview.widget.DiffUtil
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter

/**
 * <pre>
 *     author : derek
 *     time   : 2023/01/04
 *     desc   :
 *     version:
 * </pre>
 */
class SpeedPlayDialog {

    companion object {
        val selectList = mutableListOf(0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.75, 3.0)
    }

    fun showSpeedSelectDialog(
        mActivity: Activity,
        selectSpeed: Double?,
        callBack: ((Double?) -> Unit)? = null
    ) {
        val dialog = SelectDialog<Double>(mActivity)
        dialog.setTip("请选择快进倍数")
        dialog.setAdapter(object : SelectDialogAdapter.SelectDialogInterface<Double?> {
            override fun click(value: Double?, pos: Int) {
                try {
                    dialog.cancel()
                    callBack?.invoke(value)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun getDisplay(value: Double?): String {
                return value.toString()
            }
        }, object : DiffUtil.ItemCallback<Double>() {
            override fun areItemsTheSame(oldItem: Double, newItem: Double): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Double, newItem: Double): Boolean {
                return oldItem == newItem
            }
        }, selectList, selectList.indexOf(selectSpeed))
        dialog.show()
    }
}