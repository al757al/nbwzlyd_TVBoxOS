package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.util.AdapterDiffCallBack;
import com.owen.tvrecyclerview.widget.TvRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SelectDialogNew<T> extends BaseDialog {
    public SelectDialogNew(@NonNull @NotNull Context context) {
        super(context);
        setContentView(R.layout.dialog_select);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setTip(String tip) {
        ((TextView) findViewById(R.id.title)).setText(tip);
    }

    public void setAdapter(SelectDialogAdapter.SelectDialogInterface<T> sourceBeanSelectDialogInterface, AdapterDiffCallBack adapterDiffCallBack, List<T> data, int select) {

        TvRecyclerView tvRecyclerView = findViewById(R.id.list);
        SelectDialogAdapter<T> adapter = (SelectDialogAdapter<T>) tvRecyclerView.getAdapter();
        if (adapter == null) {
            adapter = new SelectDialogAdapter(sourceBeanSelectDialogInterface, null, false);
            tvRecyclerView.setAdapter(adapter);
        }
        adapter.setSelect(select);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(adapterDiffCallBack, false);
        //为了适配diffUtil才这么写的
        adapter.getData().clear();
        adapter.getData().addAll(data);
        diffResult.dispatchUpdatesTo(adapter);
        tvRecyclerView.post(() -> {
            tvRecyclerView.scrollToPositionWithOffset(select, 0, true);
        });
    }

    public List<T> getOldItems() {
        TvRecyclerView tvRecyclerView = findViewById(R.id.list);
        RecyclerView.Adapter adapter = tvRecyclerView.getAdapter();
        if (adapter == null) {
            return new ArrayList<>();
        }
        SelectDialogAdapter selectDialogAdapter = (SelectDialogAdapter) adapter;
        return selectDialogAdapter.getData();
    }


}
