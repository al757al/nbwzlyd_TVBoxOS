package com.github.tvbox.osc.util

import android.content.Context
import com.tencent.mmkv.MMKV

object KVStorage {

    private var mmkv = MMKV.defaultMMKV();

    fun init(context: Context, rootDir: String?) {
        MMKV.initialize(context, rootDir)
    }

    fun getString(key: String, defaultValue: String): String? {
        return MMKV.defaultMMKV().getString(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return MMKV.defaultMMKV().getInt(key, defaultValue)
    }

}