package com.github.tvbox.osc.bean

/**
 * <pre>
 *     author : derek
 *     time   : 2022/09/30
 *     desc   :
 *     version:
 * </pre>
 */
class LiveSourceBean: BaseItem() {
    var sourceName = ""
    var sourceUrl = ""
    var isSelected = false
    override fun getUniKey(): String {
        return (sourceUrl + sourceName).hashCode().toString()
    }
}