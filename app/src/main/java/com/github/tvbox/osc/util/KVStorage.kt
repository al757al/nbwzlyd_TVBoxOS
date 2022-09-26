package com.github.tvbox.osc.util

import com.tencent.mmkv.MMKV

object KVStorage {

    private var mmkv = MMKV.defaultMMKV();

    @JvmStatic
    fun getString(key: String, defaultValue: String): String? {
        return MMKV.defaultMMKV().getString(key, defaultValue)
    }

    @JvmStatic
    fun putString(key: String, defaultValue: String) {
        MMKV.defaultMMKV().putString(key, defaultValue)
    }

    @JvmStatic
    fun putBoolean(key: String, defaultValue: Boolean) {
        MMKV.defaultMMKV().putBoolean(key, defaultValue)
    }

    @JvmStatic
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return MMKV.defaultMMKV().getBoolean(key, defaultValue)
    }

    @JvmStatic
    fun getInt(key: String, defaultValue: Int): Int {
        return MMKV.defaultMMKV().getInt(key, defaultValue)
    }

    @JvmStatic
    fun <T> putList(key: String, list: List<T>) {
        val dataStr = GsonUtil.BeanToJson(list)
        putString(key, dataStr)
    }

    @JvmStatic
    fun <T> putBean(key: String, t: T) {
        val dataStr = GsonUtil.BeanToJson(t)
        putString(key, dataStr)
    }

    @JvmStatic
    fun <T> getBean(key: String, clazz: Class<T>): T? {
        val string = getString(key, "")
        if (string.isNullOrEmpty()) {
            return null
        }
        return GsonUtil.GsonToBean(string, clazz)
    }

    @JvmStatic
    fun <T> getList(key: String, clazz: Class<T>): MutableList<T> {
        val string = getString(key, "")
        var data = mutableListOf<T>()
        if (!string.isNullOrEmpty()) {
            data = GsonUtil.JsonToList(string, clazz)
        }
        return data
    }

    @JvmStatic
    fun deleteAll() {
        mmkv.allKeys()?.forEach {
            mmkv.remove(it)
        }
    }

}