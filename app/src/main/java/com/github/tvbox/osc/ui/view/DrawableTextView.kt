package com.github.tvbox.osc.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import com.github.tvbox.osc.R

/**
 * <pre>
 *     author : derek
 *     time   : 2022/09/15
 *     desc   :
 *     version:
 * </pre>
 */
class DrawableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var leftDrawableWidth: Int
    private var leftDrawableHeight: Int

    private var rightDrawableWidth: Int
    private var rightDrawableHeight: Int

    private var topDrawableWidth: Int
    private var topDrawableHeight: Int

    private var bottomDrawableWidth: Int
    private var bottomDrawableHeight: Int

    private var leftWidth = 0
    private var rightWidth = 0//左右图片宽度 = 0

    companion object {
        private const val DRAWABLE_LEFT = 0
        private const val DRAWABLE_TOP = 1
        private const val DRAWABLE_RIGHT = 2
        private const val DRAWABLE_BOTTOM = 3
    }

    //设置图片的高度和宽度
    private fun setDrawableSize(
        drawable: Drawable?,
        index: Int
    ) {
        if (drawable == null) {
            return
        }
        //左上右下
        var width = 0
        var height = 0
        when (index) {
            DRAWABLE_LEFT -> {
                width = leftDrawableWidth
                height = leftDrawableHeight
            }
            DRAWABLE_TOP -> {
                width = topDrawableWidth
                height = topDrawableHeight
            }
            DRAWABLE_RIGHT -> {
                width = rightDrawableWidth
                height = rightDrawableHeight
            }
            DRAWABLE_BOTTOM -> {
                width = bottomDrawableWidth
                height = bottomDrawableHeight
            }
        }

        //如果没有设置图片的高度和宽度具使用默认的图片高度和宽度
        if (width < 0) {
            width = drawable.intrinsicWidth
        }
        if (height < 0) {
            height = drawable.intrinsicHeight
        }
        if (index == 0) {
            leftWidth = width
        } else if (index == 2) {
            rightWidth = width
        }
        drawable.setBounds(0, 0, width, height)
    }

    init {
        //扩展属性
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView, defStyleAttr, 0)
        leftDrawableHeight = typedArray.getDimensionPixelSize(
            R.styleable.DrawableTextView_leftDrawableHeight,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                -1f,
                resources.displayMetrics
            ).toInt()
        )
        leftDrawableWidth = typedArray.getDimensionPixelSize(
            R.styleable.DrawableTextView_leftDrawableWidth,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                -1f,
                resources.displayMetrics
            ).toInt()
        )
        rightDrawableHeight = typedArray.getDimensionPixelSize(
            R.styleable.DrawableTextView_rightDrawableHeight,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                -1f,
                resources.displayMetrics
            ).toInt()
        )
        rightDrawableWidth = typedArray.getDimensionPixelSize(
            R.styleable.DrawableTextView_rightDrawableWidth,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                -1f,
                resources.displayMetrics
            ).toInt()
        )
        topDrawableHeight = typedArray.getDimensionPixelSize(
            R.styleable.DrawableTextView_topDrawableHeight,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                -1f,
                resources.displayMetrics
            ).toInt()
        )
        topDrawableWidth = typedArray.getDimensionPixelSize(
            R.styleable.DrawableTextView_topDrawableWidth,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                -1f,
                resources.displayMetrics
            ).toInt()
        )
        bottomDrawableHeight = typedArray.getDimensionPixelSize(
            R.styleable.DrawableTextView_bottomDrawableHeight,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                -1f,
                resources.displayMetrics
            ).toInt()
        )
        bottomDrawableWidth = typedArray.getDimensionPixelSize(
            R.styleable.DrawableTextView_bottomDrawableWidth,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                -1f,
                resources.displayMetrics
            ).toInt()
        )

        typedArray.recycle()
        val drawables = compoundDrawables
        for (i in drawables.indices) {
            setDrawableSize(drawables[i], i)
        }

        //放置图片
        setCompoundDrawables(
            drawables[DRAWABLE_LEFT],
            drawables[DRAWABLE_TOP],
            drawables[DRAWABLE_RIGHT],
            drawables[DRAWABLE_BOTTOM]
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                //top
                mDrawableTopListener?.let {
                    val drawableTop = compoundDrawables[DRAWABLE_TOP]
                    if (drawableTop != null
                        && event.y <= (top + drawableTop.bounds.height())
                        && event.y > top
                    ) {
                        it.onDrawableTop(this)
                        return true
                    }
                }
                //left
                mDrawableLeftListener?.let {
                    val drawableLeft = compoundDrawables[DRAWABLE_LEFT]
                    if (drawableLeft != null
                        && event.x <= (left + drawableLeft.bounds.width())
                        && event.x > left
                    ) {
                        it.onDrawableLeft(this)
                        return true
                    }
                }

                //right
                mDrawableRightListener?.let {
                    val drawableRight = compoundDrawables[DRAWABLE_RIGHT]
                    if (drawableRight != null
                        && event.x >= (right - drawableRight.bounds.width())
                        && event.x < right
                    ) {
                        it.onDrawableRight(this)
                        return true
                    }
                }
                //bottom
                mDrawableBottomListener?.let {
                    val drawableBottom = compoundDrawables[DRAWABLE_BOTTOM]
                    if (drawableBottom != null
                        && event.y >= (bottom - drawableBottom.bounds.height())
                        && event.y < bottom
                    ) {
                        it.onDrawableBottom(this)
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private var mDrawableTopListener: DrawableTopListener? = null
    fun setDrawableTopListener(drawableTopListener: DrawableTopListener) {
        this.mDrawableTopListener = drawableTopListener
    }

    interface DrawableTopListener {
        fun onDrawableTop(view: DrawableTextView)
    }

    private var mDrawableLeftListener: DrawableLeftListener? = null
    fun setDrawableLeftListener(drawableLeftListener: DrawableLeftListener) {
        this.mDrawableLeftListener = drawableLeftListener
    }

    interface DrawableLeftListener {
        fun onDrawableLeft(view: DrawableTextView)
    }

    private var mDrawableRightListener: DrawableRightListener? = null
    fun setDrawableRightListener(drawableRightListener: DrawableRightListener) {
        this.mDrawableRightListener = drawableRightListener
    }

    interface DrawableRightListener {
        fun onDrawableRight(view: DrawableTextView)
    }

    private var mDrawableBottomListener: DrawableBottomListener? = null
    fun setDrawableBottomListener(drawableBottomListener: DrawableBottomListener) {
        this.mDrawableBottomListener = drawableBottomListener
    }

    interface DrawableBottomListener {
        fun onDrawableBottom(view: DrawableTextView)
    }
}
