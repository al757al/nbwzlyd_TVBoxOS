package com.github.tvbox.osc.ext

import android.view.View

fun View?.letVisible() {
    if (this?.visibility == View.VISIBLE) {
        return
    }
    this?.visibility = View.VISIBLE
}

fun View?.letInVisible() {
    if (this?.visibility == View.INVISIBLE) {
        return
    }
    this?.visibility = View.INVISIBLE
}

fun View?.letGone() {
    if (this?.visibility == View.GONE) {
        return
    }
    this?.visibility = View.GONE
}

fun String?.castToInt(): Int {
    if (this.isNullOrEmpty()) {
        return 0
    }
    return this.toInt()
}

fun String?.castToLong(): Long {
    if (this.isNullOrEmpty()) {
        return 0L
    }
    return this.toLong()
}