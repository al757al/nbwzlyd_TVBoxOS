package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.owen.tvrecyclerview.widget.TvRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.jessyan.autosize.utils.AutoSizeUtils;

public class SelectDialog<T> extends BaseDialog {

    public SelectDialog(@NonNull @NotNull Context context) {
        super(context);
        setContentView(R.layout.dialog_select);
    }

    private boolean muteCheck = false;

    public SelectDialog(@NonNull @NotNull Context context, int resId) {
        super(context);
        setContentView(resId);
    }

    public void setItemCheckDisplay(boolean shouldShowCheck) {
        muteCheck = !shouldShowCheck;
    }

    public void setLayoutManger(RecyclerView.LayoutManager layoutManger) {
        int count = ((GridLayoutManager) layoutManger).getSpanCount();
        int width = AutoSizeUtils.mm2px(getContext(), 650);
        if (count == 1) {
            width = AutoSizeUtils.mm2px(getContext(), 480);
        } else if (count == 2) {

        } else if (count == 3) {
            width = AutoSizeUtils.mm2px(getContext(), 820);

        } else if (count == 4) {
            width = AutoSizeUtils.mm2px(getContext(), 980);
        }
        findViewById(R.id.rootLayout).setLayoutParams(new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        TvRecyclerView recyclerView = findViewById(R.id.list);
        int padding = AutoSizeUtils.mm2px(getContext(), 8);
        recyclerView.setPadding(padding, 0, padding, 0);
        recyclerView.setLayoutManager(layoutManger);
        if (recyclerView.getItemDecorationCount() == 0) {
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.left = AutoSizeUtils.mm2px(getContext(), 5);
                    outRect.right = AutoSizeUtils.mm2px(getContext(), 5);
//                    super.getItemOffsets(outRect, view, parent, state);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setTip(String tip) {
        ((TextView) findViewById(R.id.title)).setText(tip);
    }

    public void setAdapter(SelectDialogAdapter.SelectDialogInterface<T> sourceBeanSelectDialogInterface, DiffUtil.ItemCallback<T> sourceBeanItemCallback, List<T> data, int select) {
        SelectDialogAdapter<T> adapter = new SelectDialogAdapter(sourceBeanSelectDialogInterface, sourceBeanItemCallback, muteCheck);
        adapter.setData(data, select);
        TvRecyclerView tvRecyclerView = ((TvRecyclerView) findViewById(R.id.list));
        tvRecyclerView.setAdapter(adapter);
        tvRecyclerView.setSelectedPosition(select);
        tvRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                tvRecyclerView.smoothScrollToPosition(select);
            }
        });
    }
}
