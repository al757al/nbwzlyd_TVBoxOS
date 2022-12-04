package com.github.tvbox.osc.util;

import android.content.Intent;
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
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.CacheManager;
import com.github.tvbox.osc.player.MyVideoView;
import com.github.tvbox.osc.player.controller.FloatVodController;
import com.github.tvbox.osc.ui.activity.DetailActivity;
import com.github.tvbox.osc.ui.activity.PlayActivity;
import com.github.tvbox.osc.ui.view.ScaleImage;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;
import org.json.JSONObject;

import me.jessyan.autosize.utils.AutoSizeUtils;
import xyz.doikki.videoplayer.player.ProgressManager;

/**
 * <pre>
 *     author : derek
 *     time   : 2022/12/03
 *     desc   :
 *     version:
 * </pre>
 */
public class FloatViewUtil2 {
    private long videoDuration = 0;
    private ScaleImage scaleImage;
    private View fullScreenImage;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable runnable = new DismissRunnable();
    private final VodControllerListener listener = new VodControllerListener();

    public static final String FLOAT_TAG = "float_view";
    private FloatVodController floatVodController;

//    private MyVideoView myVideoView;

    public void openFloat(MyVideoView videoView, String progressKey, JSONObject playConfig) {
//        this.myVideoView = videoView;
        EasyFloat.dismiss(FLOAT_TAG);
        ProgressManager progressManager = new ProgressManager() {
            @Override
            public void saveProgress(String url, long progress) {
                if (videoDuration == 0) return;
                CacheManager.save(MD5.string2MD5(url), progress);
            }

            @Override
            public long getSavedProgress(String url) {
                int st = 0;
                try {
                    st = playConfig.getInt("st");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                long skip = st * 1000;
                if (CacheManager.getCache(MD5.string2MD5(url)) == null) {
                    return skip;
                }
                long rec = (long) CacheManager.getCache(MD5.string2MD5(url));
                if (rec < skip) return skip;
                return rec;
            }
        };

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
                    progressManager.saveProgress(progressKey == null ? videoView.getPlayUrl() : progressKey, videoView.getCurrentPosition());
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
                    mHandler.postDelayed(runnable, 6000);
                }

            }

            @Override
            public void drag(@NonNull View view, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void dragEnd(@NonNull View view) {

            }
        }).setLayout(R.layout.float_app_scale, view -> {
            RelativeLayout content = view.findViewById(R.id.rlContent);
//                    myVideoView = view.findViewById(R.id.mVideoView);
            floatVodController = new FloatVodController(App.getInstance());
            VodInfo vodInfo = App.getInstance().getVodInfo();
            VodInfo.VodSeries vs = vodInfo.seriesMap.get(vodInfo.playFlag).get(vodInfo.playIndex);
            floatVodController.setTitle(vodInfo.name + " " + vs.name);
            videoView.setVideoController(floatVodController);
            videoView.setProgressManager(progressManager);
            ((ViewGroup) videoView.getParent()).removeView(videoView);
            content.addView(videoView, 0);
            listener.setMyVideoView(videoView);
            floatVodController.setListener(listener);
            floatVodController.setPlayerConfig(playConfig);
//                    PlayerHelper.updateCfg(myVideoView, playConfig);
//                    myVideoView.setUrl(url);
            videoView.setProgressManager(progressManager);
            ActivityUtils.getTopActivity().moveTaskToBack(true);//将应用推到后台
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();
            scaleImage = view.findViewById(R.id.ivScale);
            scaleImage.setOnScaledListener((x, y, event) -> {
                if (params.width > ScreenUtils.getScreenWidth()) {
                    params.width = ScreenUtils.getScreenWidth();
                } else {//当宽度达到最大的时候，高度不再变化
                    params.height = (int) Math.max(params.height + x, 300);
                }
                params.width = (int) Math.max(params.width + x, 400);
                EasyFloat.updateFloat(FLOAT_TAG, -1, -1, params.width, params.height);
            });
            fullScreenImage = view.findViewById(R.id.ivClose);
            view.findViewById(R.id.ivClose).setOnClickListener(v -> {
                EasyFloat.dismiss(FLOAT_TAG);
                Intent intent = new Intent();
                intent.putExtra("isFromFloat", true);
                intent.putExtra("vodInfo", App.getInstance().getVodInfo());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Hawk.get(HawkConfig.SHOW_PREVIEW, true)) {
                    intent.setClass(App.getInstance(), DetailActivity.class);
                } else {
                    intent.setClass(App.getInstance(), PlayActivity.class);
                }
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
        }
    }

    private class VodControllerListener implements FloatVodController.VodControlListener {

        private MyVideoView myVideoView;

        public void setMyVideoView(MyVideoView videoView) {
            this.myVideoView = videoView;
        }

        @Override
        public void playNext(boolean rmProgress) {
//                            PlayFragment.this.playNext(rmProgress);
            ToastUtils.make().setGravity(Gravity.TOP, 0, 0).show("功能还在开发中");
//                            if (rmProgress && progressKey != null)
//                                CacheManager.delete(MD5.string2MD5(progressKey), 0);
        }

        @Override
        public void playPre() {
//                            PlayFragment.this.playPrevious();
            ToastUtils.make().setGravity(Gravity.TOP, 0, AutoSizeUtils.dp2px(myVideoView.getContext(), 100)).show("功能还在开发中");
        }

        @Override
        public void changeParse(ParseBean pb) {

        }

        @Override
        public void updatePlayerCfg() {
//                            mVodInfo.playerCfg = mVodPlayerCfg.toString();
//                            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_REFRESH, mVodPlayerCfg));
        }

        @Override
        public void replay(boolean replay) {
//                            autoRetryCount = 0;
            String url = myVideoView.getPlayUrl();
            myVideoView.release();
            myVideoView.setUrl(url);
            myVideoView.start();
//                            play(replay);
        }

        @Override
        public void errReplay() {
        }

        @Override
        public void selectSubtitle() {
        }

        @Override
        public void selectAudioTrack() {
        }

        @Override
        public void prepared() {
            videoDuration = myVideoView.getDuration();
        }
    }

    private void playNext(boolean isProgress) {
        VodInfo mVodInfo = App.getInstance().getVodInfo();
        boolean hasNext;
        if (mVodInfo == null || mVodInfo.seriesMap.get(mVodInfo.playFlag) == null) {
            hasNext = false;
        } else {
            hasNext = mVodInfo.playIndex + 1 < mVodInfo.seriesMap.get(mVodInfo.playFlag).size();
        }
        if (!hasNext) {
            ToastUtils.showShort("已经是最后一集了!");
            return;
        } else {
            mVodInfo.playIndex++;
        }
//        play(false);
    }
}
