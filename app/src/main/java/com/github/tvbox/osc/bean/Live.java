package com.github.tvbox.osc.bean;

import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Live {

    @SerializedName("type")
    private int type;
    @SerializedName("name")
    private String name;
    @SerializedName("group")
    private String group;
    @SerializedName("url")
    private String url;
    @SerializedName("logo")
    private String logo;
    @SerializedName("epg")
    private String epg;
    @SerializedName("ua")
    private String ua;
    @SerializedName("channels")
    private List<LiveChannelItem> channels;
    @SerializedName("groups")
    private List<LiveChannelGroup> groups;

    private boolean activated;

    public static Live objectFrom(JsonElement element) {
        return new Gson().fromJson(element, Live.class);
    }

    public Live() {
    }

    public Live(String url) {
        this.name = Uri.parse(url).getLastPathSegment();
        this.url = url;
    }

    public Live(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public String getGroup() {
        return TextUtils.isEmpty(group) ? "" : group;
    }

    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    public String getLogo() {
        return TextUtils.isEmpty(logo) ? "" : logo;
    }

    public String getEpg() {
        return TextUtils.isEmpty(epg) ? "" : epg;
    }

    public String getUa() {
        return TextUtils.isEmpty(ua) ? "" : ua;
    }

    public List<LiveChannelItem> getChannels() {
        return channels = channels == null ? new ArrayList<>() : channels;
    }

    public List<LiveChannelGroup> getGroups() {
        return groups = groups == null ? new ArrayList<>() : groups;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setActivated(Live item) {
        this.activated = item.equals(this);
    }

    public String getActivatedName() {
        return (isActivated() ? "√ " : "").concat(getName());
    }

    public Live check() {
        boolean proxy = getGroup().equals("redirect") && getChannels().size() > 0 && getChannels().get(0).getChannelUrls().size() > 0 && getChannels().get(0).getChannelUrls().get(0).startsWith("proxy");
        if (proxy) this.url = getChannels().get(0).getChannelUrls().get(0).split("ext=")[1];
        if (proxy) this.name = getChannels().get(0).getChannelName();
        return this;
    }

    public LiveChannelGroup find(LiveChannelGroup item) {
        for (LiveChannelGroup group : getGroups())
            if (group.getGroupName().equals(item.getGroupName())) return group;
        getGroups().add(item);
        return item;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Live)) return false;
        Live it = (Live) obj;
        return getName().equals(it.getName()) && getUrl().equals(it.getUrl());
    }

    public static class Sorter implements Comparator<Live> {

        @Override
        public int compare(Live live1, Live live2) {
            return Boolean.compare(live2.isActivated(), live1.isActivated());
        }
    }
}
