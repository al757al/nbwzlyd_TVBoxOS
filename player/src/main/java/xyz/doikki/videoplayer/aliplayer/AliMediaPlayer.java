package xyz.doikki.videoplayer.aliplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.AliPlayerGlobalSettings;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorCode;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.bean.InfoBean;
import com.aliyun.player.bean.InfoCode;
import com.aliyun.player.source.UrlSource;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.video.VideoSize;

import java.util.Map;

import xyz.doikki.videoplayer.player.AbstractPlayer;

/**
 * <pre>
 *     author : derek
 *     time   : 2022/12/07
 *     desc   :
 *     version:
 * </pre>
 */
public class AliMediaPlayer extends AbstractPlayer implements Player.Listener {

    private AliPlayer aliPlayer;
    private boolean isAliPlayerStart;
    private long currentPos;

    public AliMediaPlayer(Context context) {
        aliPlayer = AliPlayerFactory.createAliPlayer(context);
//        Android播放器SDK支持使用HTTP/2协议，该协议通过多路复用，避免队头阻塞，以改善播放性能。示例如下：
        AliPlayerGlobalSettings.setUseHttp2(true);
    }

    @Override
    public void initPlayer() {
        aliPlayer.setOnErrorListener(new IPlayer.OnErrorListener() {
            //此回调会在使用播放器的过程中，出现了任何错误，都会回调此接口。

            @Override
            public void onError(ErrorInfo errorInfo) {
                ErrorCode errorCode = errorInfo.getCode(); //错误码。
                String errorMsg = errorInfo.getMsg(); //错误描述。
                //出错后需要停止掉播放器。
                aliPlayer.stop();
            }
        });
        aliPlayer.setOnPreparedListener(new IPlayer.OnPreparedListener() {
            // 调用aliPlayer.prepare()方法后，播放器开始读取并解析数据。成功后，会回调此接口。

            @Override
            public void onPrepared() {
                //一般调用start开始播放视频。
                aliPlayer.start();
                isAliPlayerStart = true;
                mPlayerEventListener.onPrepared();
                // 修复播放纯音频时状态出错问题
//                if (!isVideo()) {
//                    mPlayerEventListener.onInfo(AbstractPlayer.MEDIA_INFO_RENDERING_START, 0);
//                }
            }
        });
        aliPlayer.setOnCompletionListener(new IPlayer.OnCompletionListener() {
            //播放完成之后，就会回调到此接口。
            @Override
            public void onCompletion() {
                //一般调用stop停止播放视频。
                aliPlayer.stop();
            }
        });
        aliPlayer.setOnInfoListener(new IPlayer.OnInfoListener() {
            //播放器中的一些信息，包括：当前进度、缓存位置等等。
            @Override
            public void onInfo(InfoBean infoBean) {
                InfoCode code = infoBean.getCode(); //信息码。
                String msg = infoBean.getExtraMsg();//信息内容。
                long value = infoBean.getExtraValue(); //信息值。
                currentPos = InfoCode.CurrentPosition.getValue();


                //当前进度：InfoCode.CurrentPosition
                //当前缓存位置：InfoCode.BufferedPosition
            }
        });
        aliPlayer.setOnStateChangedListener(new IPlayer.OnStateChangedListener() {
            @Override
            public void onStateChanged(int i) {
                //          int idle = 0;
                //          int initalized = 1;
                //          int prepared = 2;
                //          int started = 3;
                //          int paused = 4;
                //          int stopped = 5;
                //          int completion = 6;

                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        mPlayerEventListener.onPrepared();
                        break;
                    case 3:
                        mPlayerEventListener.onInfo(AbstractPlayer.MEDIA_INFO_RENDERING_START, 0);
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        mPlayerEventListener.onCompletion();
                        break;
                }


            }
        });
        aliPlayer.setOnLoadingStatusListener(new IPlayer.OnLoadingStatusListener() {
            //播放器的加载状态, 网络不佳时，用于展示加载画面。

            @Override
            public void onLoadingBegin() {
                //开始加载。画面和声音不足以播放。
                //一般在此处显示圆形加载。
            }

            @Override
            public void onLoadingProgress(int percent, float netSpeed) {
                //加载进度。百分比和网速。
            }

            @Override
            public void onLoadingEnd() {
                //结束加载。画面和声音可以播放。
                //一般在此处隐藏圆形加载。
            }
        });

    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        UrlSource urlSource = new UrlSource();
        urlSource.setUri(path);//播放地址，可以是第三方点播地址，或阿里云点播服务中的播放地址，也可以是本地视频地址。
        aliPlayer.setDataSource(urlSource);
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {
//        aliPlayer.setDataSource();

    }

    @Override
    public void start() {
        aliPlayer.start();

    }

    @Override
    public void pause() {
        aliPlayer.pause();

    }

    @Override
    public void stop() {
        aliPlayer.stop();

    }

    @Override
    public void prepareAsync() {
        aliPlayer.prepare();

    }

    @Override
    public void reset() {
        aliPlayer.reset();

    }

    @Override
    public boolean isPlaying() {
        return isAliPlayerStart;
    }

    @Override
    public void seekTo(long time) {
        aliPlayer.seekTo(time);

    }

    @Override
    public void release() {
        aliPlayer.release();
    }

    @Override
    public long getCurrentPosition() {
        return currentPos;
    }

    @Override
    public long getDuration() {
        return aliPlayer.getDuration();
    }

    @Override
    public int getBufferedPercentage() {
        return 0;
    }

    @Override
    public void setSurface(Surface surface) {
        aliPlayer.setSurface(surface);

    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        aliPlayer.setDisplay(holder);

    }

    @Override
    public void setVolume(float v1, float v2) {
        aliPlayer.setVolume(v1);
    }

    @Override
    public void setLooping(boolean isLooping) {
        aliPlayer.setLoop(isLooping);

    }

    @Override
    public void setOptions() {
    }

    @Override
    public void setSpeed(float speed) {
        aliPlayer.setSpeed(speed);
    }

    @Override
    public float getSpeed() {
        return aliPlayer.getSpeed();
    }

    @Override
    public long getTcpSpeed() {
        return 0;
    }

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        int videoWidth = videoSize.width;
        int videoHeight = videoSize.height;
        if (videoWidth != 0 && videoHeight != 0) {
            mPlayerEventListener.onVideoSizeChanged(videoWidth, videoHeight);
        }
    }
}
