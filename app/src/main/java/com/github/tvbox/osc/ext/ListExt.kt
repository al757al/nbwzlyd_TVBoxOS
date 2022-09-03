package com.github.tvbox.osc.ext

fun <T> MutableCollection<T>.removeFirstIf(predicate: (T) -> Boolean): Boolean {
    val iterator = this.iterator()

    while (iterator.hasNext()) {
        val next = iterator.next()
        if (predicate(next)) {
            iterator.remove()
            return true
        }
    }
    return false
}

fun <T> MutableCollection<T>.findFirst(predicate: (T) -> Boolean): T? {
    val iterator = this.iterator()

    while (iterator.hasNext()) {
        val next = iterator.next()
        if (predicate(next)) {

            return next
        }
    }
    return null
}