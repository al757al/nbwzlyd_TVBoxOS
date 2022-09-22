package com.github.tvbox.osc.bean

/**
 * 多源地址bean
 */
class MoreSourceBean : BaseItem() {
    var sourceName = ""
    var sourceUrl = ""
    var isServer = true
    var isSelected = false
    override fun getUniKey(): String {
        return (sourceUrl + sourceName).hashCode().toString()
    }
}