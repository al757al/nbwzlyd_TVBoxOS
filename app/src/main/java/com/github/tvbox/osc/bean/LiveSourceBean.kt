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

    override fun equals(obj: Any?): Boolean {
        // 首先判断传进来的obj是否是调用equals方法对象的this本身，提高判断效率
        if (obj === this) {
            return true
        }
        // 判断传进来的obj是否是null，提高判断效率
        // 判断传进来的obj是否是null，提高判断效率
        if (obj == null) {
            return false
        }
        // 判断传进来的obj是否是User对象，防止出现类型转换的异常
        // 判断传进来的obj是否是User对象，防止出现类型转换的异常
        if (obj is LiveSourceBean) {
            val liveSourceBean: LiveSourceBean = obj as LiveSourceBean
            return liveSourceBean.uniKey == uniKey
        }
        // 如果没有走类型判断语句说明两个比较的对象它们的类型都不一样，结果就是false了
        return false

    }
}