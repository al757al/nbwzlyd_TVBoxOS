package com.github.tvbox.osc.bean

/**
 * 多源地址bean
 */
class MoreSourceBean : BaseItem() {
    var sourceName = ""
    var sourceUrl = ""
    var isServer = true
    var isSelected = false
    override val uniKey: String get() = (sourceUrl + sourceName).hashCode().toString()
}