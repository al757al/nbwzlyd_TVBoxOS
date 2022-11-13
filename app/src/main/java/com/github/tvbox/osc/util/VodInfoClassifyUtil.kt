package com.github.tvbox.osc.util

import com.github.tvbox.osc.bean.VodInfo

/**
 * <pre>
 *     author : derek
 *     time   : 2022/11/10
 *     desc   :
 *     version:
 * </pre>
 */
class VodInfoClassifyUtil {
    companion object {
        @JvmStatic
        fun checkVodInfoNeedClassify(vodSeries: MutableList<VodInfo.VodSeries>?): Boolean {
            return (vodSeries?.size ?: 0) >= 40
        }

        @JvmStatic
        fun getClassifyData(
            vodSeries: MutableList<out VodInfo.VodSeries>?
        ): java.util.LinkedHashMap<VodInfo.VodSeriesFlag, List<VodInfo.VodSeries>> {
            return averageAssignFixLength(vodSeries, 30)
        }

        private fun <T> averageAssignFixLength(
            data: List<T>?,
            splitItemNum: Int
        ): LinkedHashMap<VodInfo.VodSeriesFlag, List<T>> {
            val source = ArrayList<T>()
            data?.let { source.addAll(it) }
            val result = LinkedHashMap<VodInfo.VodSeriesFlag, List<T>>()
            if (source.run { isNotEmpty() } && splitItemNum > 0) {
                if (source.size <= splitItemNum) {
                    return result
                } else {
                    // 计算拆分后list数量
                    val splitNum =
                        if (source.size % splitItemNum == 0) source.size / splitItemNum else source.size / splitItemNum + 1

                    var value: List<T>?
                    for (i in 0 until splitNum) {
                        value = if (i < splitNum - 1) {
                            source.subList(i * splitItemNum, (i + 1) * splitItemNum)
                        } else {
                            // 最后一组
                            source.subList(i * splitItemNum, source.size)
                        }
                        val vodSeriesFlag = VodInfo.VodSeriesFlag().apply {
                            var endInt = (i + 1) * 30
                            if (endInt > source.size) {
                                endInt = source.size
                            }
                            this.name = "${i * 30 + 1}-${endInt}"
                            this.selected = i == 0
                        }
                        result[vodSeriesFlag] = value
                    }
                }
            }

            return result
        }

    }
}