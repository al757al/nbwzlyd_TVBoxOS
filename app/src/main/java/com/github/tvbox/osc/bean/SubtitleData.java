package com.github.tvbox.osc.bean;

import java.io.Serializable;
import java.util.List;

public class SubtitleData implements Serializable {

    private Boolean isNew;

    private List<Subtitle> subtitleList;

    private Boolean isZip;

    public Boolean getIsNew() {
        return isNew;
    }

    public List<Subtitle> getSubtitleList() {
        return subtitleList;
    }

    public Boolean getIsZip() {
        return isZip;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    public void setSubtitleList(List<Subtitle> subtitle) {
        this.subtitleList = subtitle;
    }

    public void setIsZip(Boolean zip) {
        isZip = zip;
    }

    @Override
    public String toString() {
        return "SubtitleData{" +
                "isNew='" + isNew + '\'' +
                '}';
    }
}
