package com.github.tvbox.osc.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.tvbox.osc.R
import com.github.tvbox.osc.databinding.SettingItemBinding

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
) : FrameLayout(context, attrs) {
    private var binding: SettingItemBinding? = null
    init {
        inflate(context, R.layout.setting_item, this)
        binding = SettingItemBinding.inflate(LayoutInflater.from(getContext()), this,true)

        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SettingItem)
        val mainTitle = typedArray.getString(R.styleable.SettingItem_main_title)
        val subTitle = typedArray.getString(R.styleable.SettingItem_sub_title)
        typedArray.recycle()
        binding?.mainTitle?.text = mainTitle
        binding?.subTitle?.text = subTitle
    }

    fun setSubTitle(subTitle:String?){
        binding?.subTitle?.text = subTitle
    }
}