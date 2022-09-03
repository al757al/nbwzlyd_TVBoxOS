package com.github.tvbox.osc.util

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken


/**
 * Created by EraJieZhang on 2018/7/30.
 */
object GsonUtil {
    private var gson: Gson? = null

    /**
     * 将object对象转成json字符串
     *
     * @param object
     * @return
     */
    fun GsonToString(`object`: Any?): String? {
        var gsonString: String? = null
        if (gson != null) {
            gsonString = gson!!.toJson(`object`)
        }
        return gsonString
    }

    /**
     * 将gsonString转成泛型bean
     *
     * @param gsonString
     * @param cls
     * @return
     */
    fun <T> GsonToBean(gsonString: String?, cls: Class<T>?): T? {
        var t: T? = null
        if (gson != null) {
            t = gson!!.fromJson(gsonString, cls)
        }
        return t
    }


    /**
     * 转成list
     * 解决泛型问题
     * @param json
     * @param cls
     * @param <T>
     * @return
    </T> */
    fun <T> JsonToList(json: String?, cls: Class<T>?): MutableList<T> {
        val gson = Gson()
        val list: MutableList<T> = ArrayList()
        val array = JsonParser.parseString(json).asJsonArray
        for (elem in array) {
            list.add(gson.fromJson(elem, cls))
        }
        return list
    }

    /**
     * 转成list中有map的
     *
     * @param gsonString
     * @return
     */
    fun <T> GsonToListMaps(gsonString: String?): List<Map<String, T>>? {
        var list: List<Map<String, T>>? = null
        if (gson != null) {
            list = gson!!.fromJson(
                gsonString,
                object : TypeToken<List<Map<String?, T>?>?>() {}.getType()
            )
        }
        return list
    }

    /**
     * 转成map的
     *
     * @param gsonString
     * @return
     */
    fun <T> GsonToMaps(gsonString: String?): Map<String, T>? {
        var map: Map<String, T>? = null
        if (gson != null) {
            map = gson!!.fromJson(gsonString, object : TypeToken<Map<String?, T>?>() {}.type)
        }
        return map
    }

    /**
     * 把一个bean（或者其他的字符串什么的）转成json
     * @param object
     * @return
     */
    fun BeanToJson(`object`: Any?): String {
        return gson!!.toJson(`object`)
    }

    init {
        if (gson == null) {
            gson = Gson()
        }
    }
}