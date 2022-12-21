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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private ArrayList<LiveChannelGroup> liveChannelGroupList;

    public void openFloat(VideoView videoView, LiveChannelItem curLiveChannelItem, ArrayList<LiveChannelGroup> liveChannelGroupList, int chanelGroupIndex) {
        EasyFloat.dismiss(FLOAT_TAG);
        this.videoView = videoView;
        this.liveChannelGroupList = liveChannelGroupList;
        this.currentLiveChannelItem = curLiveChannelItem;
        this.chanelGroupIndex = chanelGroupIndex;

        Activity topActivity = ActivityUtils.getTopActivity();
        EasyFloat.Builder builder = new EasyFloat.Builder(topActivity);
        builder.setLandScape(topActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        EasyFloat.with(App.getInstance().getApplicationContext()).setTag(FLOAT_TAG).setShowPattern(ShowPattern.BACKGROUND).setLocation(0, 0).registerCallbacks(new OnFloatCallbacks() {
            @Override
            public void createdResult(boolean b, @Nullable String s, @Nullable View view) {

            }

            @Override
            public void show(@NonNull View view) {
                videoView.requestLayout();
                videoView.resume();
                if (floatVodController != null) {
                    floatVodController.setListener(listener);
                }

            }

            @Override
            public void hide(@NonNull View view) {

            }

            @Override
            public void dismiss() {
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
            floatVodController = new LiveFloatController(App.getInstance());
            videoView.setVideoController(floatVodController);
            ((ViewGroup) videoView.getParent()).removeView(videoView);
            content.addView(videoView, 0);
            floatVodController.setListener(listener);
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
                intent.putExtra("currentLiveChannelItem", this.currentLiveChannelItem);
                intent.putExtra("currentChannelGroupIndex", this.chanelGroupIndex);
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
                    mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
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
            playNextChanel(videoView, liveChannelGroupList);
        }

        @Override
        public void preChanel() {
            playPreChanel(videoView, liveChannelGroupList);

        }
    }

    private final Runnable mConnectTimeoutChangeSourceRun = new Runnable() {
        @Override
        public void run() {
            playNextChanel(videoView, liveChannelGroupList);
        }
    };

    private void playNextChanel(VideoView videoView, ArrayList<LiveChannelGroup> liveChannelGroupList) {
        ToastUtils.make().setGravity(Gravity.TOP, 0, 100).show("切换下一个频道");
        videoView.release();
        int nextPlayPos = 0;
        int curGroupIndex = 0;
        for (int i = 0; i < liveChannelGroupList.size(); i++) {
            ArrayList<LiveChannelItem> liveChannels = liveChannelGroupList.get(i).getLiveChannels();
            for (int j = 0; j < liveChannels.size(); j++) {
                if (currentLiveChannelItem.equals(liveChannels.get(j))) {
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

        if (curGroupIndex >= liveChannelGroupList.size()) {
            ToastUtils.make().setGravity(Gravity.TOP, 0, 100).show("没有频道了");
            return;
        }
        ArrayList<LiveChannelItem> liveChannels = liveChannelGroupList.get(curGroupIndex).getLiveChannels();
        this.currentLiveChannelItem = liveChannels.get(nextPlayPos);
        this.chanelGroupIndex = curGroupIndex;
        playUrl(currentLiveChannelItem);
    }


    private void playPreChanel(VideoView videoView, ArrayList<LiveChannelGroup> liveChannelGroupList) {
        ToastUtils.make().setGravity(Gravity.TOP, 0, 100).show("切换上一个频道");
        videoView.release();
        int nextPlayPos = 0;
        int curGroupIndex = 0;
        for (int i = 0; i < liveChannelGroupList.size(); i++) {
            ArrayList<LiveChannelItem> liveChannels = liveChannelGroupList.get(i).getLiveChannels();
            for (int j = 0; j < liveChannels.size(); j++) {
                if (currentLiveChannelItem.equals(liveChannels.get(j))) {
                    int curPos = j;
                    curGroupIndex = i;
                    curPos -= 1;
                    if (curPos < 0) {//如果超过了，换组
                        curGroupIndex--;
                        nextPlayPos = 0;
                        if (curGroupIndex < 0) {
                            ToastUtils.make().setGravity(Gravity.TOP, 0, 100).show("没有频道了");
                            return;
                        }
                    } else {
                        nextPlayPos = curPos;
                    }
                }
            }
        }

        ArrayList<LiveChannelItem> preLiveChannels = liveChannelGroupList.get(curGroupIndex).getLiveChannels();
        if (preLiveChannels.isEmpty()) {
            ToastUtils.make().setGravity(Gravity.TOP, 0, 100).show("没有频道了");
            return;
        }
        currentLiveChannelItem = preLiveChannels.get(nextPlayPos);
        this.chanelGroupIndex = curGroupIndex;
        playUrl(currentLiveChannelItem);
    }


    public ExecutorService executor;

    public void playUrl(LiveChannelItem item) {
        execute(() -> {
            String url = item.getUrl();
            if (!item.isForceTv()) {
                return url;
            }
            url = Force.get().fetch(url);
            return url;
        });
    }

    private void execute(Callable<?> callable) {
        if (executor != null) executor.shutdownNow();
        executor = Executors.newFixedThreadPool(2);
        executor.execute(() -> {
            try {
                if (!Thread.interrupted()) {
                    Object o = executor.submit(callable).get(30, TimeUnit.SECONDS);
                    if (o instanceof String) {
                        mHandler.post(() -> {
                            videoView.setUrl((String) o);
                            videoView.start();
                        });
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }


}
