package com.github.tvbox.osc.util

class StringUtils {

    companion object {
        @JvmStatic
        fun isStrEmpty(str: String?): Boolean {
            return str.isNullOrEmpty()
        }
    }
}