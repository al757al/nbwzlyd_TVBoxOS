package com.github.tvbox.osc.ui.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.TextView
import androidx.core.view.forEach
import com.github.tvbox.osc.R
import com.github.tvbox.osc.util.PlayerHelper
import com.github.tvbox.osc.util.ScreenUtils
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

    private var mScaleBtn: TextView? = null
    private var mSpeedBtn: TextView? = null
    private var mAudioTrack: TextView? = null
    private var mLandscapePortraitBtn: TextView? = null

    init {
        setContentView(R.layout.player_more_fuc_pop)
        setBackgroundColor(Color.TRANSPARENT)
        mScaleBtn = findViewById(R.id.play_scale)
        mSpeedBtn = findViewById(R.id.play_speed)
        mAudioTrack = findViewById(R.id.audio_track_select)
        mLandscapePortraitBtn = findViewById(R.id.landscape_portrait)
        mLandscapePortraitBtn?.setOnClickListener {
            setLandscapePortrait()
        }
        playConfig?.let {
            mScaleBtn?.text = PlayerHelper.getScaleName(it.getInt("sc"))
            mSpeedBtn?.text = "x" + playConfig.getDouble("sp")
            mAudioTrack?.visibility = if (it.getInt("pl") == 1) View.VISIBLE else View.GONE
            initLandscapePortraitBtnInfo()
        }
        initLandscapePortraitBtnInfo()

    }

    fun setOnItemClickListener(onClick: ((view: View?) -> Unit)? = null): PlayerMoreFucPop {
        (contentView as? ViewGroup)?.forEach {
            it.setOnClickListener { childView ->
                onClick?.invoke(childView)
            }
        }
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
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            mLandscapePortraitBtn?.text = "竖屏"
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

}