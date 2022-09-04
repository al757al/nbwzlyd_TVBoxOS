package com.github.tvbox.osc.util;

public class HistoryHelper {
    private static Integer[] hisNumArray = {30, 50, 70, 100, 200};

    public static String getHistoryNumName(int index) {
        Integer value = getHisNum(index);
        return value + "条";
    }

    public static int getHisNum(int index) {
        Integer value = null;
        if (index >= 0 && index < hisNumArray.length) {
            value = hisNumArray[index];
        } else {
            value = hisNumArray[0];
        }
        return value;
    }
}
