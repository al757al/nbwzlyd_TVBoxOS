package com.github.tvbox.osc.util;

import com.blankj.utilcode.util.LogUtils;
import com.google.android.exoplayer2.C;
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

    public static void getTrackSelector(TrackSelector trackSelector) {
        if (trackSelector instanceof DefaultTrackSelector) {
            DefaultTrackSelector mapTrackSelector = (DefaultTrackSelector) trackSelector;
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = mapTrackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                    TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(i);
                    if (C.TRACK_TYPE_AUDIO == mappedTrackInfo.getRendererType(i)) { //判断是否是音轨
                        for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
                            TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
                            LogUtils.d("checkAudio", trackGroup.getFormat(0).language);
                            mapTrackSelector.setParameters(
                                    mapTrackSelector.getParameters().buildUpon()
                                            .setPreferredAudioLanguage(trackGroup.getFormat(0).language));
                        }
                    } else if (C.TRACK_TYPE_TEXT == mappedTrackInfo.getRendererType(i)) { //判断是否是字幕
                        for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
                            TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
                            mapTrackSelector.setParameters(
                                    mapTrackSelector.getParameters().buildUpon()
                                            .setPreferredTextLanguage(trackGroup.getFormat(0).language));//这个方法就是字幕轨道
                        }

                    }
                }
            }
        }
    }
}
