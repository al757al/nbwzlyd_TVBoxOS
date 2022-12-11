package com.github.tvbox.osc.util;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.bean.LiveChannelGroup;
import com.github.tvbox.osc.bean.LiveChannelItem;
import com.github.tvbox.osc.player.controller.LiveController;
import com.github.tvbox.osc.player.controller.LiveFloatController;
import com.github.tvbox.osc.ui.view.ScaleImage;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import xyz.doikki.videoplayer.player.VideoView;

/**
 * <pre>
 *     author : derek
 *     time   : 2022/12/03
 *     desc   :
 *     version:
 * </pre>
 */
public class LiveFloatViewUtil {
    private long videoDuration = 0;
    private ScaleImage scaleImage;
    private View fullScreenImage;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable runnable = new DismissRunnable();
    private final VodControllerListener listener = new VodControllerListener();

    public static final String FLOAT_TAG = "float_view";
    private LiveFloatController floatVodController;
    private LiveChannelItem currentLiveChannelItem;
    private VideoView videoView;
    private int chanelGroupIndex;

//    private MyVideoView myVideoView;

    public void openFloat(VideoView videoView, LiveChannelItem currentLiveChannelItem, int chanelGroupIndex) {
        this.videoView = videoView;
        Activity topActivity = ActivityUtils.getTopActivity();
        EasyFloat.dismiss(FLOAT_TAG);
        this.currentLiveChannelItem = currentLiveChannelItem;
        this.chanelGroupIndex = chanelGroupIndex;
        EasyFloat.Builder builder = new EasyFloat.Builder(topActivity);
        builder.setLandScape(topActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        EasyFloat.with(App.getInstance().getApplicationContext()).setTag(FLOAT_TAG).setShowPattern(ShowPattern.BACKGROUND).setLocation(100, 100).registerCallbacks(new OnFloatCallbacks() {
            @Override
            public void createdResult(boolean b, @Nullable String s, @Nullable View view) {

            }

            @Override
            public void show(@NonNull View view) {
                videoView.requestLayout();
                videoView.resume();
                if (floatVodController != null) {
                    listener.setMyVideoView(videoView);
                    floatVodController.setListener(listener);
                }

            }

            @Override
            public void hide(@NonNull View view) {

            }

            @Override
            public void dismiss() {
                if (videoDuration > 0 && videoView != null) {
                    videoView.setVideoController(null);
                }
//                videoView.release();
                if (floatVodController != null) {
                    floatVodController.setListener(null);
                }
                mHandler.removeCallbacksAndMessages(null);
            }

            @Override
            public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {
                mHandler.removeCallbacks(runnable);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (scaleImage != null) {
                        scaleImage.setVisibility(View.VISIBLE);

                    }
                    if (fullScreenImage != null) {
                        fullScreenImage.setVisibility(View.VISIBLE);
                    }
                    if (floatVodController != null) {
                        floatVodController.setPreNextBtnVisibility(true);
                    }
                }
                mHandler.postDelayed(runnable, 6000);

            }

            @Override
            public void drag(@NonNull View view, @NonNull MotionEvent motionEvent) {
                mHandler.removeCallbacks(runnable);

            }

            @Override
            public void dragEnd(@NonNull View view) {
                mHandler.postDelayed(runnable, 6000);

            }
        }).setLayout(R.layout.float_app_scale, view -> {
            RelativeLayout content = view.findViewById(R.id.rlContent);
//                    myVideoView = view.findViewById(R.id.mVideoView);
            floatVodController = new LiveFloatController(App.getInstance());
            videoView.setVideoController(floatVodController);
            ((ViewGroup) videoView.getParent()).removeView(videoView);
            content.addView(videoView, 0);
            listener.setMyVideoView(videoView);
            floatVodController.setListener(listener);
//            floatVodController.setPlayerConfig(playConfig);
//                    PlayerHelper.updateCfg(myVideoView, playConfig);
//                    myVideoView.setUrl(url);
            topActivity.moveTaskToBack(true);//将应用推到后台
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();
            scaleImage = view.findViewById(R.id.ivScale);
            scaleImage.setOnScaledListener((x, y, event) -> {
                if (params.width > ScreenUtils.getScreenWidth()) {
                    params.width = ScreenUtils.getScreenWidth();
                } else {//当宽度达到最大的时候，高度不再变化
                    params.height = (int) Math.max(params.height + x, 270);
                }
                params.width = (int) Math.max(params.width + x, 480);
                EasyFloat.updateFloat(FLOAT_TAG, -1, -1, params.width, params.height);
            });
            fullScreenImage = view.findViewById(R.id.ivClose);
            view.findViewById(R.id.ivClose).setOnClickListener(v -> {
                EasyFloat.dismiss(FLOAT_TAG);
                Intent intent = new Intent();
                intent.putExtra("isFromFloat", true);
                intent.putExtra("currentLiveChannelItem", currentLiveChannelItem);
                intent.putExtra("currentChannelGroupIndex", chanelGroupIndex);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(App.getInstance(), topActivity.getClass());
                App.getInstance().startActivity(intent);
            });
        }).show();
    }

    public class DismissRunnable implements Runnable {

        @Override
        public void run() {
            if (scaleImage != null) {
                scaleImage.setVisibility(View.GONE);
            }
            if (fullScreenImage != null) {
                fullScreenImage.setVisibility(View.GONE);
            }
            if (floatVodController != null) {
                floatVodController.setPreNextBtnVisibility(false);
            }
        }
    }

    private class VodControllerListener implements LiveController.LiveControlListener {

        private VideoView myVideoView;

        public void setMyVideoView(VideoView videoView) {
            this.myVideoView = videoView;
        }

        @Override
        public boolean singleTap(MotionEvent e) {
            return false;
        }

        @Override
        public void longPress() {

        }

        @Override
        public void playStateChanged(int playState) {

            switch (playState) {
                case VideoView.STATE_IDLE:
                case VideoView.STATE_PAUSED:
                    break;
                case VideoView.STATE_PREPARED:
                case VideoView.STATE_BUFFERED:
                case VideoView.STATE_PLAYING:
//                    currentLiveChangeSourceTimes = 0;
                    mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
//                    sBar.setMax((int) mVideoView.getDuration());
//                    tv_currentpos.setText(durationToString((int) mVideoView.getCurrentPosition()));
//                    tv_duration.setText(durationToString((int) mVideoView.getDuration()));
//                    liveController.hideProgressContainer();
                    break;
                case VideoView.STATE_ERROR:
                case VideoView.STATE_PLAYBACK_COMPLETED:
                    mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                    mHandler.post(mConnectTimeoutChangeSourceRun);
                    break;
                case VideoView.STATE_PREPARING:
                case VideoView.STATE_BUFFERING:
                    mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                    mHandler.postDelayed(mConnectTimeoutChangeSourceRun, (Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 5) + 1) * 5000);
                    break;
            }

        }

        @Override
        public void changeSource(int direction) {

        }

        @Override
        public void nextChanel() {
            playNextChanel(videoView);
        }

        @Override
        public void preChanel() {
            playPreChanel(videoView);

        }
    }

    private final Runnable mConnectTimeoutChangeSourceRun = new Runnable() {
        @Override
        public void run() {
            playNextChanel(videoView);
        }
    };

    private void playNextChanel(VideoView videoView) {
        videoView.release();
        int nextPlayPos = 0;
        int curGroupIndex = 0;
        List<LiveChannelGroup> channelGroupList = ApiConfig.get().getChannelGroupList();
        for (int i = 0; i < channelGroupList.size(); i++) {
            ArrayList<LiveChannelItem> liveChannels = channelGroupList.get(i).getLiveChannels();
            for (int j = 0; j < liveChannels.size(); j++) {
                if (currentLiveChannelItem == liveChannels.get(j)) {
                    int curPos = j;
                    curGroupIndex = i;
                    curPos += 1;
                    if (curPos >= liveChannels.size()) {//如果超过了，换组
                        curGroupIndex++;
                        nextPlayPos = 0;
                    } else {
                        nextPlayPos = curPos;
                    }
                }
            }
        }
        currentLiveChannelItem =
                channelGroupList.get(curGroupIndex).getLiveChannels().get(nextPlayPos);
        this.chanelGroupIndex = curGroupIndex;
        videoView.setUrl(currentLiveChannelItem.getUrl());
        videoView.start();
//        if (curGroupIndex < channelGroupList.size() && nextPlayPos < channelGroupList.get(curGroupIndex).getLiveChannels().size()) {
//
//        } else {
//            ToastUtils.make().setGravity(Gravity.CENTER, 100, 100).show("没有频道了");
//        }
    }


    private void playPreChanel(VideoView videoView) {
        videoView.release();
        int nextPlayPos = 0;
        int curGroupIndex = 0;
        List<LiveChannelGroup> channelGroupList = ApiConfig.get().getChannelGroupList();
        for (int i = 0; i < channelGroupList.size(); i++) {
            ArrayList<LiveChannelItem> liveChannels = channelGroupList.get(i).getLiveChannels();
            for (int j = 0; j < liveChannels.size(); j++) {
                if (currentLiveChannelItem == liveChannels.get(j)) {
                    int curPos = j;
                    curGroupIndex = i;
                    curPos -= 1;
                    if (curPos < 0) {//如果超过了，换组
                        curGroupIndex--;
                        nextPlayPos = 0;
                        if (curGroupIndex < 0) {
                            ToastUtils.make().setGravity(Gravity.CENTER, 100, 100).show("没有频道了");
                            return;
                        }
                    } else {
                        nextPlayPos = curPos;
                    }
                }
            }
        }

        currentLiveChannelItem =
                channelGroupList.get(curGroupIndex).getLiveChannels().get(nextPlayPos);
        this.chanelGroupIndex = curGroupIndex;
        videoView.setUrl(currentLiveChannelItem.getUrl());
        videoView.start();
    }

}
