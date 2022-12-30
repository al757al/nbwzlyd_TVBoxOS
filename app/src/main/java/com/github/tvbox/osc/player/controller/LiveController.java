package com.github.tvbox.osc.player.controller;

import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;

import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 直播控制器
 */

public class LiveController extends BaseController {
    protected ProgressBar mLoading;
    private int minFlingDistance = 150;             //最小识别距离
    private int minFlingVelocity = 2000;              //最小识别速度
    private ImageView mProgressIcon;
    private TextView mProgressText;
    private View mProgressContainer;

    public LiveController(@NotNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.player_live_control_view;
    }

    @Override
    protected void initView() {
        super.initView();
        mLoading = findViewById(R.id.loading);
        mProgressIcon = findViewById(R.id.tv_progress_icon);
        mProgressText = findViewById(R.id.tv_progress_text);
        mProgressContainer = findViewById(R.id.tv_progress_container);
        setForceImmersive(Hawk.get(HawkConfig.IMMERSIVE_SWITCH, false));
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (listener.singleTap(e))
            return true;
        return super.onSingleTapConfirmed(e);
    }

    private LiveController.LiveControlListener listener = null;

    public void setListener(LiveController.LiveControlListener listener) {
        this.listener = listener;
    }

    public interface LiveControlListener {
        boolean singleTap(MotionEvent e);

        void longPress();

        void playStateChanged(int playState);

        void changeSource(int direction);

        void nextChanel();

        void preChanel();
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        return super.onKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        listener.longPress();
        super.onLongPress(e);
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        listener.playStateChanged(playState);
    }

    @Override
    protected void stopSlide() {
        super.stopSlide();
        hideProgressContainer();

    }

    public void hideProgressContainer() {
        if (mProgressContainer.getVisibility() == VISIBLE) {
            new Handler().postDelayed(() -> mProgressContainer.setVisibility(View.INVISIBLE), 1000);
        }
    }

    @Override
    public void updateSeekUI(int curr, int seekTo, int duration) {
        if (mProgressContainer.getVisibility() != VISIBLE) {
            mProgressContainer.setVisibility(View.VISIBLE);
        }
        if (seekTo > curr) {
            mProgressIcon.setImageResource(R.drawable.icon_pre);
        } else {
            mProgressIcon.setImageResource(R.drawable.icon_back);
        }
        mProgressText.setText(PlayerUtils.stringForTime(seekTo) + " / " + PlayerUtils.stringForTime(duration));
        mHandler.sendEmptyMessage(1000);
        mHandler.removeMessages(1001);
        mHandler.sendEmptyMessageDelayed(1001, 1000);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > minFlingDistance && Math.abs(velocityX) > minFlingVelocity) {
            listener.changeSource(-1);          //左滑
        } else if (e2.getX() - e1.getX() > minFlingDistance && Math.abs(velocityX) > minFlingVelocity) {
            listener.changeSource(1);           //右滑
        } else if (e1.getY() - e2.getY() > minFlingDistance && Math.abs(velocityY) > minFlingVelocity && enableFling()) {
            listener.nextChanel();
        } else if (e2.getY() - e1.getY() > minFlingDistance && Math.abs(velocityY) > minFlingVelocity && enableFling()) {
            listener.preChanel();
        }
        return false;
    }
}
