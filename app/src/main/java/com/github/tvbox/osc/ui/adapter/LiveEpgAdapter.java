package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.Epginfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LiveEpgAdapter extends BaseQuickAdapter<Epginfo, BaseViewHolder> {
    private int selectedEpgIndex = -1;
    private int focusedEpgIndex = -1;
    public static float fontSize = 20;
    private int defaultShiyiSelection = 0;
    private boolean ShiyiSelection = false;
    private String shiyiDate = null;
    private String currentEpgDate = null;
    private int focusSelection = -1;
    private boolean source_include_back = false;

    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");

    public LiveEpgAdapter() {
        super(R.layout.epglist_item, new ArrayList<>());
    }

    public void CanBack(Boolean source_include_back) {
        this.source_include_back = source_include_back;
    }

    @Override
    protected void convert(BaseViewHolder holder, Epginfo value) {
        TextView textview = holder.getView(R.id.tv_epg_name);
        TextView timeview = holder.getView(R.id.tv_epg_time);
        TextView shiyi = holder.getView(R.id.shiyi);
        if (value.index == selectedEpgIndex && value.index != focusedEpgIndex && (value.currentEpgDate.equals(shiyiDate) || value.currentEpgDate.equals(timeFormat.format(new Date())))) {
            textview.setTextColor(mContext.getResources().getColor(R.color.color_1890FF));
            timeview.setTextColor(mContext.getResources().getColor(R.color.color_1890FF));
        } else {
            textview.setTextColor(Color.WHITE);
            timeview.setTextColor(Color.WHITE);
        }
        if (new Date().compareTo(value.startdateTime) >= 0 && new Date().compareTo(value.enddateTime) <= 0) {
            shiyi.setVisibility(View.VISIBLE);
            shiyi.setBackgroundColor(Color.YELLOW);
            shiyi.setText("直播中");
            shiyi.setTextColor(Color.RED);
        } else if (new Date().compareTo(value.enddateTime) > 0 && source_include_back) {
            shiyi.setVisibility(View.VISIBLE);
            shiyi.setBackgroundColor(Color.BLUE);
            shiyi.setTextColor(Color.WHITE);
            shiyi.setText("回看");
        } else if (new Date().compareTo(value.startdateTime) < 0) {
            shiyi.setVisibility(View.VISIBLE);
            shiyi.setBackgroundColor(Color.GRAY);
            shiyi.setTextColor(Color.BLACK);
            shiyi.setText("预约");
        } else {
            shiyi.setVisibility(View.GONE);
        }
        textview.setText(value.title);
        timeview.setText(value.start + "--" + value.end);
        Log.e("roinlong", "getView: " + selectedEpgIndex);
        if (!ShiyiSelection) {
            Date now = new Date();
            if (now.compareTo(value.startdateTime) >= 0 && now.compareTo(value.enddateTime) <= 0) {
                textview.setFreezesText(true);
                timeview.setFreezesText(true);
            }
        } else {
            if (value.index == this.selectedEpgIndex && value.currentEpgDate.equals(shiyiDate)) {
                textview.setFreezesText(true);
                timeview.setFreezesText(true);
                shiyi.setText("回看中");
                shiyi.setTextColor(Color.RED);
                shiyi.setBackgroundColor(Color.rgb(12, 255, 0));
                if (new Date().compareTo(value.startdateTime) >= 0 && new Date().compareTo(value.enddateTime) <= 0) {
                    shiyi.setVisibility(View.VISIBLE);
                    shiyi.setBackgroundColor(Color.YELLOW);
                    shiyi.setText("直播中");
                    shiyi.setTextColor(Color.RED);
                }
            }
        }

    }

    public void setShiyiSelection(int i, boolean t, String currentEpgDate) {
        this.selectedEpgIndex = i;
        this.shiyiDate = t ? currentEpgDate : null;
        ShiyiSelection = t;
        notifyItemChanged(this.selectedEpgIndex);

    }

    public int getSelectedIndex() {
        return selectedEpgIndex;
    }

    public void setSelectedEpgIndex(int selectedEpgIndex) {
        if (selectedEpgIndex == this.selectedEpgIndex) return;
        int preSelectedIndex = this.selectedEpgIndex;
        this.selectedEpgIndex = selectedEpgIndex;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (preSelectedIndex != -1)
                    notifyItemChanged(preSelectedIndex);
                if (selectedEpgIndex != -1)
                    notifyItemChanged(selectedEpgIndex);
            }
        });
    }

    public int getFocusedEpgIndex() {
        return focusedEpgIndex;
    }

    public void setFocusedEpgIndex(int focusedEpgIndex) {
        //cannot call this method while RecyclerView is computing a layout or scrolling com.owen.tvrecyclerview.widget.TvRecyclerView{946921a GFE..V... ......ID 765,0-1744,1080 #7f080148 app:id/lv_epg}, adapter:com.github.tvbox.osc.ui.adapter.LiveEpgAdapter@bfb0495, layout:com.owen.tvrecyclerview.widget.
        // V7LinearLayoutManager@58489aa, context:com.github.tvbox.osc.ui.activity.LivePlayActivity@59687f3


        int preSelectedIndex = this.selectedEpgIndex;
        this.focusedEpgIndex = focusedEpgIndex;
        new Handler().post(() -> {
            if (preSelectedIndex != -1)
                notifyItemChanged(preSelectedIndex);
            else if (focusedEpgIndex != -1)
                notifyItemChanged(focusedEpgIndex);
        });

    }
}
