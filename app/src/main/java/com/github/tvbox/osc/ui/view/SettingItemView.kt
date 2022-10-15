package com.github.tvbox.osc.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.tvbox.osc.R
import com.github.tvbox.osc.databinding.SettingItemBinding
import me.jessyan.autosize.utils.AutoSizeUtils

/**
 * <pre>
 *     author : derek
 *     time   : 2022/10/14
 *     desc   :
 *     version:
 * </pre>
 */
class SettingItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private var binding: SettingItemBinding? = null
    private val size = AutoSizeUtils.mm2px(context,20f);

    init {
//        inflate(context, R.layout.setting_item, this)

        layoutParams = ViewGroup.LayoutParams(
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                AutoSizeUtils.mm2px(context, 60f)
            )
        )
        setPadding(size,0,size,0)
        gravity=Gravity.CENTER_VERTICAL
        binding = SettingItemBinding.inflate(LayoutInflater.from(getContext()), this)

        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SettingItemView)
        val mainTitle = typedArray.getString(R.styleable.SettingItemView_main_title)
        val subTitle = typedArray.getString(R.styleable.SettingItemView_sub_title)
        typedArray.recycle()
        binding?.mainTitle?.text = mainTitle
        binding?.subTitle?.text = subTitle
    }

    fun setSubTitle(subTitle: String?) {
        binding?.subTitle?.text = subTitle
    }
}