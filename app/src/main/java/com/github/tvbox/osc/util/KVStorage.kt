//package com.github.tvbox.osc.util
//
//import com.orhanobut.hawk.Hawk
//
//object KVStorage {
//
//    @JvmStatic
//    fun getString(key: String, defaultValue: String): String? {
//        return Hawk.get(key, defaultValue)
//    }
//
//    @JvmStatic
//    fun putString(key: String, defaultValue: String) {
//        Hawk.put(key, defaultValue)
//    }
//
//    @JvmStatic
//    fun putBoolean(key: String, defaultValue: Boolean) {
//        Hawk.put(key, defaultValue)
//    }
//
//    @JvmStatic
//    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
//        return Hawk.get(key, defaultValue)
//    }
//
//    @JvmStatic
//    fun getInt(key: String, defaultValue: Int): Int {
//        return Hawk.get(key, defaultValue)
//    }
//
//    @JvmStatic
//    fun putInt(key: String, defaultValue: Int) {
//        Hawk.put(key, defaultValue)
//    }
//
//    @JvmStatic
//    fun <T> putList(key: String, list: List<T>) {
//        Hawk.put(key,list)
//    }
//
//    @JvmStatic
//    fun <T> putBean(key: String, t: T) {
//        Hawk.put(key,t)
//    }
//
//    @JvmStatic
//    fun <T> getBean(key: String, clazz: Class<T>): T? {
//        return Hawk.get(key,null)
//    }
//
//    @JvmStatic
//    fun <T> getList(key: String, clazz: Class<T>): MutableList<T> {
////        val string = getString(key, "")
////        var data = mutableListOf<T>()
////        if (!string.isNullOrEmpty()) {
////            data = GsonUtil.JsonToList(string, clazz)
////        }
////
//        return Hawk.get(key,ArrayList<clazz>())
//    }
//
//    @JvmStatic
//    fun deleteAll() {
//        Hawk.deleteAll()
//    }
//
//    fun remove(key: String) {
//        Hawk.delete(key)
//    }
//
//}