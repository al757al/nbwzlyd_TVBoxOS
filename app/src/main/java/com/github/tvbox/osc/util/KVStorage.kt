package com.github.tvbox.osc.util

import com.orhanobut.hawk.Hawk

object KVStorage {

    @JvmStatic
    fun getString(key: String, defaultValue: String): String? {
        return Hawk.get(key, defaultValue)
    }

    @JvmStatic
    fun putString(key: String, defaultValue: String) {
        Hawk.put(key, defaultValue)
    }

    @JvmStatic
    fun putBoolean(key: String, defaultValue: Boolean) {
        Hawk.put(key, defaultValue)
    }

    @JvmStatic
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return Hawk.get(key, defaultValue)
    }

    @JvmStatic
    fun getInt(key: String, defaultValue: Int): Int {
        return Hawk.get(key, defaultValue)
    }

    @JvmStatic
    fun putInt(key: String, defaultValue: Int) {
        Hawk.put(key, defaultValue)
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
        Hawk.deleteAll()
    }

    fun remove(key: String) {
        Hawk.delete(key)
    }

}