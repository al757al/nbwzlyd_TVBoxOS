package xyz.doikki.videoplayer.ijk;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;
import xyz.doikki.videoplayer.player.AbstractPlayer;
import xyz.doikki.videoplayer.player.VideoViewManager;

public class IjkPlayer extends AbstractPlayer implements IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnVideoSizeChangedListener, IjkMediaPlayer.OnNativeInvokeListener {

    protected IjkMediaPlayer mMediaPlayer;
    private int mBufferedPercent;
    private final Context mAppContext;

    private static final int codec = tv.danmaku.ijk.media.player.IjkMediaPlayer.OPT_CATEGORY_CODEC;
    private static final int format = tv.danmaku.ijk.media.player.IjkMediaPlayer.OPT_CATEGORY_FORMAT;
    private static final int player = tv.danmaku.ijk.media.player.IjkMediaPlayer.OPT_CATEGORY_PLAYER;

    public IjkPlayer(Context context) {
        mAppContext = context;
    }

    @Override
    public void initPlayer() {
        mMediaPlayer = new IjkMediaPlayer();
        //native日志
        IjkMediaPlayer.native_setLogLevel(VideoViewManager.getConfig().mIsEnableLog ? IjkMediaPlayer.IJK_LOG_INFO : IjkMediaPlayer.IJK_LOG_SILENT);
        setOptions();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnNativeInvokeListener(this);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 5);   //丢帧  是在视频帧处理不过来的时候丢弃一些帧达到同步的效果
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);  //设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
//        //播放延时的解决方案
//        //如果是rtsp协议，可以优先用tcp(默认是用udp)
////        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
//        mMediaPlayer.setOption(1, "analyzemaxduration", 100L);
//        mMediaPlayer.setOption(1, "flush_packets", 1L);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);   //需要准备好后自动播放
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast", 1);//不额外优化
//        mMediaPlayer.setOption(4, "packet-buffering",  0);  //是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);  //自动旋屏
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);   //处理分辨率变化
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 1024 * 3);//最大缓冲大小,单位kb
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 2);   //默认最小帧数2
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 3);   //最大缓存时长
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);   //是否限制输入缓存数
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
//        mMediaPlayer.setOption(1, "probesize", 200);  //播放前的探测Size，默认是1M, 改小一点会出画面更快
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 5);  //播放重连次数
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);//清空DNS
    }


    @Override
    public void setOptions() {

        //开启内置字幕
        mMediaPlayer.setOption(tv.danmaku.ijk.media.player.IjkMediaPlayer.OPT_CATEGORY_PLAYER, "subtitle", 1);
        mMediaPlayer.setOption(tv.danmaku.ijk.media.player.IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        mMediaPlayer.setOption(tv.danmaku.ijk.media.player.IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);

        mMediaPlayer.setOption(codec, "skip_loop_filter", 48);
        mMediaPlayer.setOption(format, "fflags", "fastseek");
        mMediaPlayer.setOption(format, "http-detect-range-support", 0);
        mMediaPlayer.setOption(player, "enable-accurate-seek", 0);
        mMediaPlayer.setOption(player, "framedrop", 1);
        mMediaPlayer.setOption(player, "max-buffer-size", 15 * 1024 * 1024);
        mMediaPlayer.setOption(player, "opensles", 0);
        mMediaPlayer.setOption(player, "overlay-format", tv.danmaku.ijk.media.player.IjkMediaPlayer.SDL_FCC_RV32);
        mMediaPlayer.setOption(player, "reconnect", 1);
        mMediaPlayer.setOption(player, "soundtouch", 1);
        mMediaPlayer.setOption(player, "start-on-prepared", 1);
    }

    public void setIjkCode(int code) {
        mMediaPlayer.setOption(player, "mediacodec", code);
        mMediaPlayer.setOption(player, "mediacodec-auto-rotate", code);
        mMediaPlayer.setOption(player, "mediacodec-handle-resolution-change", code);
        mMediaPlayer.setOption(player, "mediacodec-hevc", code);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            Uri uri = Uri.parse(path);
            if (uri.getScheme() != null && uri.getScheme().startsWith("rtsp")) {
                mMediaPlayer.setOption(format, "infbuf", 1);
                mMediaPlayer.setOption(format, "rtsp_transport", "tcp");
                mMediaPlayer.setOption(format, "rtsp_flags", "prefer_tcp");
            }
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())) {
                RawDataSourceProvider rawDataSourceProvider = RawDataSourceProvider.create(mAppContext, uri);
                mMediaPlayer.setDataSource(rawDataSourceProvider);
            } else {
                //处理UA问题
                if (headers != null) {
                    String userAgent = headers.get("User-Agent");
                    if (!TextUtils.isEmpty(userAgent)) {
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", userAgent);
                        // 移除header中的User-Agent，防止重复
                        headers.remove("User-Agent");
                    }
                }
                mMediaPlayer.setDataSource(mAppContext, uri, headers);
            }
        } catch (Exception e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public String getPlayUrl() {
        return mMediaPlayer.getDataSource();
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {
        try {
            mMediaPlayer.setDataSource(new RawDataSourceProvider(fd));
        } catch (Exception e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void pause() {
        try {
            mMediaPlayer.pause();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void start() {
        try {
            mMediaPlayer.start();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void stop() {
        try {
            mMediaPlayer.stop();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void prepareAsync() {
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void reset() {
        mMediaPlayer.reset();
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        setOptions();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        try {
            mMediaPlayer.seekTo((int) time);
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void release() {
        mMediaPlayer.setOnErrorListener(null);
        mMediaPlayer.setOnCompletionListener(null);
        mMediaPlayer.setOnInfoListener(null);
        mMediaPlayer.setOnBufferingUpdateListener(null);
        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.setOnVideoSizeChangedListener(null);
        new Thread() {
            @Override
            public void run() {
                try {
                    mMediaPlayer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getBufferedPercentage() {
        return mBufferedPercent;
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    @Override
    public void setVolume(float v1, float v2) {
        mMediaPlayer.setVolume(v1, v2);
    }

    @Override
    public void setLooping(boolean isLooping) {
        mMediaPlayer.setLooping(isLooping);
    }

    @Override
    public void setSpeed(float speed) {
        mMediaPlayer.setSpeed(speed);
    }

    @Override
    public float getSpeed() {
        return mMediaPlayer.getSpeed(0);
    }

    @Override
    public long getTcpSpeed() {
        return mMediaPlayer.getTcpSpeed();
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        mPlayerEventListener.onError();
        return true;
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        mPlayerEventListener.onCompletion();
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        mPlayerEventListener.onInfo(what, extra);
        return true;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        mBufferedPercent = percent;
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        mPlayerEventListener.onPrepared();
        // 修复播放纯音频时状态出错问题
        if (!isVideo()) {
            mPlayerEventListener.onInfo(AbstractPlayer.MEDIA_INFO_RENDERING_START, 0);
        }
    }

    private boolean isVideo() {
        IjkTrackInfo[] trackInfo = mMediaPlayer.getTrackInfo();
        if (trackInfo == null) return false;
        for (IjkTrackInfo info : trackInfo) {
            if (info.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        if (videoWidth != 0 && videoHeight != 0) {
            mPlayerEventListener.onVideoSizeChanged(videoWidth, videoHeight);
        }
    }

    @Override
    public boolean onNativeInvoke(int what, Bundle args) {
        return true;
    }
}
