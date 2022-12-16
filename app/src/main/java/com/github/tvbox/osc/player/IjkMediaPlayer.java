package com.github.tvbox.osc.player;

import android.content.Context;
import android.text.TextUtils;

import com.github.tvbox.osc.bean.IJKCode;

import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;
import xyz.doikki.videoplayer.ijk.IjkPlayer;

public class IjkMediaPlayer extends IjkPlayer {

    private IJKCode curDecodec = null;

    public IjkMediaPlayer(Context context, IJKCode codec) {
        super(context);
        this.curDecodec = codec;
    }

    @Override
    public void setOptions() {
        super.setOptions();
        int curCode = 0;//软解
        if (curDecodec != null) {
            if ("硬解码".equals(curDecodec.getName())) {
                curCode = 1;
            }
        }
        setIjkCode(curCode);
//        IJKCode codecTmp = this.curDecodec == null ? ApiConfig.get().getCurrentIJKCode() : this.curDecodec;
//        LinkedHashMap<String, String> options = codecTmp.getOption();
//        if (options != null) {
//            for (String key : options.keySet()) {
//                String value = options.get(key);
//                String[] opt = key.split("\\|");
//                int category = Integer.parseInt(opt[0].trim());
//                String name = opt[1].trim();
//                try {
//                    long valLong = Long.parseLong(value);
//                    mMediaPlayer.setOption(category, name, valLong);
//                } catch (Exception e) {
//                    mMediaPlayer.setOption(category, name, value);
//                }
//            }
//        }
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            if (path != null && !TextUtils.isEmpty(path) && path.startsWith("rtsp")) {
                mMediaPlayer.setOption(1, "infbuf", 1);
                mMediaPlayer.setOption(1, "rtsp_transport", "tcp");
                mMediaPlayer.setOption(1, "rtsp_flags", "prefer_tcp");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setDataSource(path, headers);
    }

    public TrackInfo getTrackInfo() {
        IjkTrackInfo[] trackInfo = mMediaPlayer.getTrackInfo();
        if (trackInfo == null) return null;
        TrackInfo data = new TrackInfo();
        int subtitleSelected = mMediaPlayer.getSelectedTrack(ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT);
        int audioSelected = mMediaPlayer.getSelectedTrack(ITrackInfo.MEDIA_TRACK_TYPE_AUDIO);
        int index = 0;
        for (IjkTrackInfo info : trackInfo) {
            if (info.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_AUDIO) {//音轨信息
                TrackInfoBean t = new TrackInfoBean();
                t.name = info.getInfoInline();
                t.language = info.getLanguage();
                t.index = index;
                t.selected = index == audioSelected;
                data.addAudio(t);
            }
            if (info.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {//内置字幕
                TrackInfoBean t = new TrackInfoBean();
                t.name = info.getInfoInline();
                t.language = info.getLanguage();
                t.index = index;
                t.selected = index == subtitleSelected;
                data.addSubtitle(t);
            }
            index++;
        }
        return data;
    }

    @Override
    public String getPlayUrl() {
        return mMediaPlayer.getDataSource();
    }

    public void setTrack(int trackIndex) {
        mMediaPlayer.selectTrack(trackIndex);
    }

    public void setOnTimedTextListener(IMediaPlayer.OnTimedTextListener listener) {
        mMediaPlayer.setOnTimedTextListener(listener);
    }

}
