package com.github.tvbox.osc.bean

/**
 * <pre>
 *     author : derek
 *     time   : 2022/09/30
 *     desc   :
 *     version:
 * </pre>
 */
class LiveSourceBean : BaseItem() {
    var sourceName = ""
    var sourceUrl = ""
    var isSelected = false
    var isOfficial = false
    override val uniKey: String
        get() = (sourceUrl + sourceName).hashCode().toString()

}