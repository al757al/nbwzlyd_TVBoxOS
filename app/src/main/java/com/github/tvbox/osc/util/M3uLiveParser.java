package com.github.tvbox.osc.util;


import android.text.TextUtils;

import com.github.tvbox.osc.bean.LiveChannelGroup;
import com.github.tvbox.osc.bean.LiveChannelItem;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3uLiveParser {

    private static final Pattern GROUP = Pattern.compile(".*group-title=\"(.?|.+?)\".*");
    private static final Pattern LOGO = Pattern.compile(".*tvg-logo=\"(.?|.+?)\".*");
    private static final Pattern NAME = Pattern.compile(".*,(.+?)$");

    private static String extract(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line.trim());
        if (matcher.matches()) return matcher.group(1);
        return "";
    }

    public static List<LiveChannelGroup> start(String m3uString) {
        if (m3uString.trim().startsWith("#EXTM3U"))
            return m3u(m3uString);
        else return new ArrayList<>();
    }


    private static List<LiveChannelGroup> m3u(String text) {
        LiveChannelItem liveChannelItem = new LiveChannelItem();
        LinkedHashMap<String, LiveChannelGroup> linkedHashMap = new LinkedHashMap<>();
        for (String line : text.split("\n")) {
            if (line.startsWith("#EXTINF:")) {
                String groupName = extract(line, GROUP);
                if (TextUtils.isEmpty(groupName)) {
                    groupName = "默认分组";
                }
                LiveChannelGroup liveChannelGroup = linkedHashMap.get(groupName);
                if (liveChannelGroup == null) {
                    liveChannelGroup = new LiveChannelGroup();
                }
                liveChannelGroup.setGroupName(groupName);
                String channelName = extract(line, NAME);
                liveChannelItem = new LiveChannelItem();
                liveChannelItem.setChannelName(channelName);
                if (!liveChannelGroup.getLiveChannels().contains(liveChannelItem)) {
                    liveChannelItem.setChannelIndex(liveChannelGroup.getLiveChannels().size());
                    liveChannelGroup.getLiveChannels().add(liveChannelItem);
                    liveChannelItem.setChannelNum(liveChannelGroup.getLiveChannels().size());
                }
                linkedHashMap.put(groupName, liveChannelGroup);
            } else if (line.contains("://")) {
                liveChannelItem.getChannelUrls().add(line);
                liveChannelItem.setChannelUrls(liveChannelItem.getChannelUrls());

            }
        }

        ArrayList<LiveChannelGroup> resultData = new ArrayList<>();
        LiveChannelGroup collectedGroup = Hawk.get(HawkConfig.LIVE_CHANELE_COLLECTD, new LiveChannelGroup());
        collectedGroup.setGroupName("收藏频道");
        collectedGroup.isCollected = true;
        resultData.add(0, collectedGroup);
        int groupIndex = 0;
        for (LiveChannelGroup liveChannelGroup : linkedHashMap.values()) {
            liveChannelGroup.setGroupIndex(++groupIndex);
            resultData.add(liveChannelGroup);
        }
        return resultData;
    }

//    private static List<LiveChannelGroup> txt(String text) {
//        for (String line : text.split("\n")) {
//            String[] split = line.split(",");
//            if (split.length < 2) continue;
//            if (line.contains("#genre#")) {
//                live.getGroups().add(Group.create(split[0]));
//            }
//            if (split[1].contains("://")) {
//                Group group = live.getGroups().get(live.getGroups().size() - 1);
//                group.find(Channel.create(split[0]).epg(live)).addUrls(split[1].split("#"));
//            }
//        }
//    }

//    private static String getText(String url) {
//        try {
//            if (url.startsWith("file")) return FileUtil.read(url);
//            else if (url.startsWith("http")) return OKHttp.newCall(url).execute().body().string();
//            else if (url.endsWith(".txt") || url.endsWith(".m3u"))
//                return getText(Utils.convert(LiveConfig.getUrl(), url));
//            else if (url.length() > 0 && url.length() % 4 == 0)
//                return getText(new String(Base64.decode(url, Base64.DEFAULT)));
//            else return "";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
//    }
}
