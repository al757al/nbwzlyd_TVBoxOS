package com.github.tvbox.osc.util

import com.tencent.mmkv.MMKV

object KVStorage {

    private var mmkv = MMKV.defaultMMKV();

    fun getString(key: String, defaultValue: String): String? {
        return MMKV.defaultMMKV().getString(key, defaultValue)
    }

    fun putString(key: String, defaultValue: String) {
        MMKV.defaultMMKV().putString(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return MMKV.defaultMMKV().getInt(key, defaultValue)
    }

    fun <T> putList(key: String, list: List<T>) {
        val dataStr = GsonUtil.BeanToJson(list)
        putString(key, dataStr)
    }

    fun <T> putBean(key: String, t: T) {
        val dataStr = GsonUtil.BeanToJson(t)
        putString(key, dataStr)
    }

    fun <T> getBean(key: String, clazz: Class<T>): T? {
        val string = getString(key, "")
        if (string.isNullOrEmpty()) {
            return null
        }
        return GsonUtil.GsonToBean(string, clazz)
    }

    fun <T> getList(key: String, clazz: Class<T>): MutableList<T> {
        val string = getString(key, "")
        var data = mutableListOf<T>()
        if (!string.isNullOrEmpty()) {
            data = GsonUtil.JsonToList(string, clazz)
        }
        return data
    }

}