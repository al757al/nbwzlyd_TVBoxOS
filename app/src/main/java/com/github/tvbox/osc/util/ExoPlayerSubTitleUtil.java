package com.github.tvbox.osc.util;

import com.github.tvbox.osc.player.TrackInfo;
import com.github.tvbox.osc.player.TrackInfoBean;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;

/**
 * <pre>
 *     author : derek
 *     time   : 2022/12/06
 *     desc   :
 *     version:
 * </pre>
 */
public class ExoPlayerSubTitleUtil {
    private static TrackInfo trackInfo;

    public static void initTrackSelector(TrackSelector trackSelector, TrackInfoCallBack trackInfoCallBack) {
        trackInfo = new TrackInfo();
        if (trackSelector instanceof DefaultTrackSelector) {
            DefaultTrackSelector mapTrackSelector = (DefaultTrackSelector) trackSelector;
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = mapTrackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                    TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                    if (C.TRACK_TYPE_AUDIO == mappedTrackInfo.getRendererType(i)) { //判断是否是音轨
                        for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
                            TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
                            TrackInfoBean audioTrackInfo = new TrackInfoBean();
                            Format format = trackGroup.getFormat(0);
                            audioTrackInfo.index = groupIndex;
                            audioTrackInfo.name = format.label;
                            audioTrackInfo.language = format.language;
                            trackInfo.addAudio(audioTrackInfo);
//                            LogUtils.d("checkAudio", trackGroup.getFormat(0).language);
//                            mapTrackSelector.setParameters(
//                                    mapTrackSelector.getParameters().buildUpon()
//                                            .setPreferredAudioLanguage(trackGroup.getFormat(0).language));
                        }
                    } else if (C.TRACK_TYPE_TEXT == mappedTrackInfo.getRendererType(i)) { //判断是否是字幕
                        for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
                            TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
                            TrackInfoBean titleTrackInfo = new TrackInfoBean();
                            Format format = trackGroup.getFormat(0);
                            titleTrackInfo.index = groupIndex;
                            titleTrackInfo.name = format.label;
                            titleTrackInfo.language = format.language;
                            trackInfo.addSubtitle(titleTrackInfo);
//                            mapTrackSelector.setParameters(
//                                    mapTrackSelector.getParameters().buildUpon()
//                                            .setPreferredTextLanguage(trackGroup.getFormat(0).language));//这个方法就是字幕轨道
                        }
                    }
                }
                trackInfoCallBack.onTrackInfoGet(trackInfo);
            }
        }
    }

    public interface TrackInfoCallBack {
        void onTrackInfoGet(TrackInfo trackInfo);
    }


    public static TrackInfo getTrackInfo() {
        return trackInfo;
    }
}
