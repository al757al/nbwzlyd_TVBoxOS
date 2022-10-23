package com.github.tvbox.osc.ui.adapter

import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.tvbox.osc.R
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.bean.VodInfo
import com.github.tvbox.osc.ext.letVisible
import com.github.tvbox.osc.picasso.RoundTransformation
import com.github.tvbox.osc.util.DefaultConfig
import com.github.tvbox.osc.util.MD5
import com.squareup.picasso.Picasso
import me.jessyan.autosize.utils.AutoSizeUtils

/**
 * @author pj567
 * @date :2020/12/21
 * @description:
 */
class HistoryAdapter : BaseQuickAdapter<VodInfo, BaseViewHolder>(R.layout.item_grid, ArrayList()) {
    override fun convert(helper: BaseViewHolder, item: VodInfo) {
        val tvYear = helper.getView<TextView>(R.id.tvYear)
        tvYear.text = ApiConfig.get().getSource(item.sourceKey).name
        tvYear.letVisible()
        helper.setVisible(R.id.tvLang, false)
        helper.setVisible(R.id.tvArea, false)
        helper.setVisible(R.id.tvNote, false)
        helper.setText(R.id.tvName, item.name)
        // helper.setText(R.id.tvActor, item.actor);
        val ivThumb = helper.getView<ImageView>(R.id.ivThumb)
        //由于部分电视机使用glide报错
        if (!TextUtils.isEmpty(item.pic)) {
            Picasso.get()
                .load(DefaultConfig.checkReplaceProxy(item.pic))
                .transform(
                    RoundTransformation(MD5.string2MD5(item.pic + item.name))
                        .centerCorp(true)
                        .override(
                            AutoSizeUtils.mm2px(mContext, 300f),
                            AutoSizeUtils.mm2px(mContext, 400f)
                        )
                        .roundRadius(
                            AutoSizeUtils.mm2px(mContext, 10f),
                            RoundTransformation.RoundType.ALL
                        )
                )
                .placeholder(R.drawable.img_loading_placeholder)
                .error(R.drawable.img_loading_placeholder)
                .into(ivThumb)
        } else {
            ivThumb.setImageResource(R.drawable.img_loading_placeholder)
        }
    }
}