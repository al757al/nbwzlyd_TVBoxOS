package com.github.tvbox.osc.ui.adapter;

import android.annotation.SuppressLint;
import android.view.Gravity;
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
import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.MoreSourceBean;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.ScreenUtils;
import com.github.tvbox.osc.util.StringUtils;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
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
        ImageView tvDel = holder.itemView.findViewById(R.id.tvDel);

        if (value instanceof MoreSourceBean && !ScreenUtils.isTv(textView.getContext())) {
            tvCopyView.setVisibility(View.VISIBLE);
        } else {
            tvCopyView.setVisibility(View.GONE);
        }
        if (value instanceof MoreSourceBean && !((MoreSourceBean) value).isServer()) {
            tvDel.setVisibility(View.VISIBLE);
        } else {
            tvDel.setVisibility(View.GONE);
        }

        if (position == select) {
            textView.setText(SpanUtils.with(textView).
                    appendImage(ContextCompat.getDrawable(textView.getContext(), R.drawable.ic_select_fill)).append(" ").append(name).create());
        } else {
            textView.setText(name);
        }
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
        tvDel.setOnClickListener(v -> {
            if (value instanceof MoreSourceBean) {
                ArrayList<MoreSourceBean> list = Hawk.get(HawkConfig.API_HISTORY_LIST, new ArrayList<>());
                Iterator<MoreSourceBean> iterator = list.iterator();
                if (iterator.hasNext()) {
                    MoreSourceBean next = iterator.next();
                    if (next.getSourceUrl().equals(((MoreSourceBean) value).getSourceUrl()) && next.getSourceName().equals(((MoreSourceBean) value).getSourceName())) {
                        iterator.remove();
                    }
                }
                getData().remove(value);
                notifyItemRemoved(position);
                notifyItemChanged(position);
                Hawk.put(HawkConfig.API_HISTORY_LIST, list);
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("删除成功");
            }
        });
    }
}
