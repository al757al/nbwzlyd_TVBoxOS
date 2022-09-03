package com.github.tvbox.osc.bean

import java.io.Serializable

/**
 * 多源地址bean
 */
class MoreSourceBean : Serializable {
    var sourceName = ""
    var sourceUrl = ""
    var isServer = true
    var isSelected = false
}