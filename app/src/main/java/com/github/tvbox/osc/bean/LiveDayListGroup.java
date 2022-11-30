package com.github.tvbox.osc.bean;

import java.io.Serializable;

public class LiveDayListGroup implements Serializable {
    private int groupIndex;
    private String groupName;


    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}
