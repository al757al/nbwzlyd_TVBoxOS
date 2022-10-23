package com.github.tvbox.osc.ui.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ToastUtils

/**
 * <pre>
 *     author : derek
 *     time   : 2022/10/23
 *     desc   :
 *     version:
 * </pre>
 */
class TvViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    private val mTempRect = Rect()
    private var lastStartTime = System.currentTimeMillis()
    private var shouldIntercept = false;

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    var currentFocused = findFocus()
                    if (currentFocused === this) {
                        currentFocused = null
                    } else if (currentFocused != null) {
                        var isChild = false
                        var parent = currentFocused.parent
                        while (parent is ViewGroup) {
                            if (parent === this) {
                                isChild = true
                                break
                            }
                            parent = parent.getParent()
                        }
                        if (!isChild) {
                            currentFocused = null
                        }
                    }

                    val nextFocused = FocusFinder.getInstance().findNextFocus(
                        this, currentFocused, View.FOCUS_RIGHT
                    )
                    val nextLeft = getChildRectInPagerCoordinates(mTempRect, nextFocused)?.left
                    val currLeft = getChildRectInPagerCoordinates(mTempRect, currentFocused)?.left
                    return if (currentFocused != null && (nextLeft ?: 0) <= (currLeft ?: 0)) {
                        if (System.currentTimeMillis() - lastStartTime < 2000) {
                            return if (shouldIntercept) {
                                true
                            } else {
                                super.dispatchKeyEvent(event)
                            }
                        } else {
                            if (currentItem != this.childCount - 1) {
                                lastStartTime = System.currentTimeMillis()
                                shouldIntercept = true
                                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("再按一次切换Tab")
                            } else {
                                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0)
                                    .show("已经到最后一个tab了")
                            }
                            true
                        }
                    } else {
                        super.dispatchKeyEvent(event);
                    }
                }
                KeyEvent.ACTION_UP -> {
                    shouldIntercept = false
                }
            }
        }
        return super.dispatchKeyEvent(event)

    }

    private fun getChildRectInPagerCoordinates(outRect: Rect, child: View?): Rect? {
        var outRect: Rect? = outRect
        if (outRect == null) {
            outRect = Rect()
        }
        if (child == null) {
            outRect[0, 0, 0] = 0
            return outRect
        }
        outRect.left = child.left
        outRect.right = child.right
        outRect.top = child.top
        outRect.bottom = child.bottom
        var parent = child.parent
        while (parent is ViewGroup && parent !== this) {
            val group = parent
            outRect.left += group.left
            outRect.right += group.right
            outRect.top += group.top
            outRect.bottom += group.bottom
            parent = group.parent
        }
        return outRect
    }
}