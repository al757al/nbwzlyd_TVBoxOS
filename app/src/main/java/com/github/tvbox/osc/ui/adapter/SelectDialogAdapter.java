package com.github.tvbox.osc.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SpanUtils;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.MoreSourceBean;
import com.github.tvbox.osc.util.ScreenUtils;
import com.github.tvbox.osc.util.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SelectDialogAdapter<T> extends ListAdapter<T, SelectDialogAdapter.SelectViewHolder> {

    private final boolean muteCheck;

    class SelectViewHolder extends RecyclerView.ViewHolder {

        public SelectViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }
    }

    public interface SelectDialogInterface<T> {
        void click(T value, int pos);

        String getDisplay(T val);
    }

    private ArrayList<T> data = new ArrayList<>();

    private int select = 0;

    private SelectDialogInterface dialogInterface = null;

    public SelectDialogAdapter(SelectDialogInterface dialogInterface, DiffUtil.ItemCallback diffCallback, boolean muteCheck) {
        super(diffCallback);
        this.dialogInterface = dialogInterface;
        this.muteCheck = muteCheck;
    }

    public void setData(List<T> newData, int defaultSelect) {
        data.clear();
        data.addAll(newData);
        select = defaultSelect;
        notifyDataSetChanged();
    }

    public void setSelect(int select) {
        this.select = select;
    }

    public ArrayList<T> getData() {
        return data;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public SelectDialogAdapter.SelectViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new SelectDialogAdapter.SelectViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_select, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SelectDialogAdapter.SelectViewHolder holder, @SuppressLint("RecyclerView") int position) {
        T value = data.get(position);
        String name = dialogInterface.getDisplay(value);
        TextView textView = holder.itemView.findViewById(R.id.tvName);
        ImageView tvCopyView = holder.itemView.findViewById(R.id.tvCopy);

        if (value instanceof MoreSourceBean && !ScreenUtils.isTv(textView.getContext())) {
            tvCopyView.setVisibility(View.VISIBLE);
        } else {
            tvCopyView.setVisibility(View.GONE);
        }

        if (position == select) {
            textView.setText(SpanUtils.with(textView).
                    appendImage(ContextCompat.getDrawable(textView.getContext(), R.drawable.ic_select_fill)).append(" ").append(name).create());
            holder.itemView.requestFocus();
        } else {
            textView.setText(name);
            holder.itemView.clearFocus();
        }
        holder.itemView.setFocusable(true);
        textView.setOnClickListener(v -> {
            if (position == select)
                return;
            notifyItemChanged(select);
            select = position;
            notifyItemChanged(select);
            dialogInterface.click(value, position);
        });

        tvCopyView.setOnClickListener(v -> {
            if (value instanceof MoreSourceBean) {
                String copyText = ((MoreSourceBean) value).getSourceName() + "\n" + ((MoreSourceBean) value).getSourceUrl();
                StringUtils.copyText(textView.getContext(), copyText);
            }

        });
    }
}
