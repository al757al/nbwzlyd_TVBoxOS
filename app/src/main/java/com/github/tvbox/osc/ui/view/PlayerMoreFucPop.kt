package com.github.tvbox.osc.ui.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import com.github.tvbox.osc.R
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.IDMDownLoadUtil
import com.github.tvbox.osc.util.KVStorage.getBoolean
import com.github.tvbox.osc.util.KVStorage.putBoolean
import com.github.tvbox.osc.util.PlayerHelper
import com.github.tvbox.osc.util.ScreenUtils
import com.orhanobut.hawk.Hawk
import org.json.JSONObject
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AnimationHelper
import razerdp.util.animation.TranslationConfig

/**
 * <pre>
 *     author : derek
 *     time   : 2022/10/21
 *     desc   :
 *     version:
 * </pre>
 */
class PlayerMoreFucPop(context: Context?, private val playConfig: JSONObject?) :
    BasePopupWindow(context) {

    private var mIdmDownLoad: TextView? = null
    private var mShowTime: TextView? = null
    private var onClick: ((view: TextView?) -> Unit)? = null
    private var mScaleBtn: TextView? = null
    private var mTinyProgress: TextView? = null
    private var mAudioTrack: TextView? = null
    private var mLandscapePortraitBtn: TextView? = null
    private val dismissRunnable: DismissRunnable by lazy {
        DismissRunnable()
    }

    private val mHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    init {
        setContentView(R.layout.player_more_fuc_pop)
        setBackgroundColor(Color.TRANSPARENT)
        mScaleBtn = findViewById(R.id.play_scale)
        mTinyProgress = findViewById(R.id.tiny_progress)
        mAudioTrack = findViewById(R.id.audio_track_select)
        mLandscapePortraitBtn = findViewById(R.id.landscape_portrait)
        mIdmDownLoad = findViewById(R.id.idm_download)
        mShowTime = findViewById(R.id.time_show)
        mLandscapePortraitBtn?.setOnClickListener {
            setLandscapePortrait()
        }
        playConfig?.let {
            mScaleBtn?.text = PlayerHelper.getScaleName(it.getInt("sc"))
//            mSpeedBtn?.text = "x" + playConfig.getDouble("sp")
            mAudioTrack?.visibility = if (it.getInt("pl") == 1) View.VISIBLE else View.GONE
            initLandscapePortraitBtnInfo()
            mScaleBtn?.text = PlayerHelper.getScaleName(it.getInt("sc"))
        }
        mScaleBtn?.setOnClickListener {
            onClick?.invoke(it as TextView?)
        }
        mTinyProgress?.text = if (Hawk.get(HawkConfig.MINI_PROGRESS)) "迷你进度开" else "迷你进度关"
        mTinyProgress?.setOnClickListener {
            onClick?.invoke(it as TextView?)
        }
        mAudioTrack?.setOnClickListener {
            onClick?.invoke(it as TextView?)
        }
        mIdmDownLoad?.setOnClickListener {
            IDMDownLoadUtil().startIDMDownLoad(getContext())
        }

        setTimeShowOrDismiss(mShowTime)
        mShowTime?.setOnClickListener { v: View? ->
            var isTimeShow = mShowTime?.tag as Int == 1
            if (isTimeShow) {
                mShowTime?.tag = 2
            } else {
                mShowTime?.tag = 1
            }
            isTimeShow = !isTimeShow
            mShowTime?.text = if (isTimeShow) "屏显开" else "屏显关"
            putBoolean(HawkConfig.VIDEO_SHOW_TIME, isTimeShow)
        }
        initLandscapePortraitBtnInfo()

    }

    fun setOnItemClickListener(onClick: ((view: TextView?) -> Unit)? = null): PlayerMoreFucPop {
        this.onClick = onClick
        return this
    }

    override fun onCreateShowAnimation(): Animation {
        return AnimationHelper.asAnimation().withTranslation(TranslationConfig.FROM_RIGHT).toShow()

    }

    override fun onCreateDismissAnimation(): Animation {
        return AnimationHelper.asAnimation().withTranslation(TranslationConfig.TO_RIGHT)
            .toDismiss()
    }


    private fun initLandscapePortraitBtnInfo() {
        val screenSqrt = ScreenUtils.getSqrt(context)
        if (screenSqrt < 20.0) {
            mLandscapePortraitBtn?.visibility = View.VISIBLE
            mLandscapePortraitBtn?.text = "竖屏"
        }
    }


    private fun setLandscapePortrait() {
        val requestedOrientation: Int = context.requestedOrientation
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            mLandscapePortraitBtn?.text = "横屏"
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            mLandscapePortraitBtn?.text = "竖屏"
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    private fun setTimeShowOrDismiss(timeShow: TextView?) {
        val isTimeShowOpen = getBoolean(HawkConfig.VIDEO_SHOW_TIME, false)
        if (isTimeShowOpen) {
            timeShow?.text = "屏显开"
            timeShow?.tag = 1
        } else {
            timeShow?.text = "屏显关"
            timeShow?.tag = 2
        }
    }

    override fun onDispatchKeyEvent(event: KeyEvent?): Boolean {
        when (event?.action) {
            KeyEvent.ACTION_DOWN -> {
                mHandler.removeCallbacksAndMessages(null)
            }
            KeyEvent.ACTION_UP -> {
                mHandler.postDelayed(dismissRunnable, 3000)
            }
        }
        return super.onDispatchKeyEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mHandler.removeCallbacksAndMessages(null)
            }
            MotionEvent.ACTION_UP -> {
                mHandler.postDelayed(dismissRunnable, 3000)
            }
        }
        return super.onTouchEvent(event)
    }


    inner class DismissRunnable : Runnable {
        override fun run() {
            this@PlayerMoreFucPop.dismiss()
        }

    }

    override fun onShowing() {
        super.onShowing()
        mHandler.postDelayed({
            this.dismiss()
        }, 3000)
    }

    override fun onDismiss() {
        super.onDismiss()
        mHandler.removeCallbacksAndMessages(null)
    }

}