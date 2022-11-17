package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.MovieSort;
import com.github.tvbox.osc.ui.adapter.GridFilterKVAdapter;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import java.util.ArrayList;

import razerdp.basepopup.BasePopupWindow;

public class GridFilterDialog extends BasePopupWindow {
    private LinearLayout filterRoot;

    public GridFilterDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_grid_filter);
        filterRoot = findViewById(R.id.filterRoot);
        setBackgroundColor(Color.TRANSPARENT);
        setPopupGravity(Gravity.BOTTOM);
    }


    @Override
    public void onWindowFocusChanged(View popupDecorViewProxy, boolean hasWindowFocus) {
        super.onWindowFocusChanged(popupDecorViewProxy, hasWindowFocus);
        filterRoot.requestFocus();
    }

    @Override
    protected Animation onCreateDismissAnimation() {
        return super.onCreateDismissAnimation();
    }

    @Override
    protected Animation onCreateShowAnimation() {
        return super.onCreateShowAnimation();
    }

//    override fun onWindowFocusChanged(popupDecorViewProxy: View?, hasWindowFocus: Boolean) {
//        super.onWindowFocusChanged(popupDecorViewProxy, hasWindowFocus)
//        mGridView?.isFocusable = true
//        mGridView?.requestFocus()
//    }
//
//
//    override fun onCreateShowAnimation(): Animation {
//        return AnimationHelper.asAnimation().withTranslation(TranslationConfig.FROM_LEFT).toShow()
//
//    }
//
//    override fun onCreateDismissAnimation(): Animation {
//        return AnimationHelper.asAnimation().withTranslation(TranslationConfig.TO_LEFT)
//                .toDismiss()
//    }


//    public GridFilterDialog(@NonNull @NotNull Context context) {
//        super(context);
//        setCanceledOnTouchOutside(true);
//        setCancelable(true);
//        setContentView(R.layout.dialog_grid_filter);
//        filterRoot = findViewById(R.id.filterRoot);
//        findViewById(R.id.outRoot).setOnClickListener(v -> dismiss());
//
//    }

    public interface Callback {
        void change();
    }

    public void setOnDismiss(Callback callback) {
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (selectChange) {
                    callback.change();
                }
            }
        });
    }

    public void setData(MovieSort.SortData sortData) {
        ArrayList<MovieSort.SortFilter> filters = sortData.filters;
        for (MovieSort.SortFilter filter : filters) {
            View line = LayoutInflater.from(getContext()).inflate(R.layout.item_grid_filter, null);
            ((TextView) line.findViewById(R.id.filterName)).setText(filter.name);
            TvRecyclerView gridView = line.findViewById(R.id.mFilterKv);
            gridView.setHasFixedSize(true);
            gridView.setLayoutManager(new V7LinearLayoutManager(getContext(), 0, false));
            GridFilterKVAdapter filterKVAdapter = new GridFilterKVAdapter();
            gridView.setAdapter(filterKVAdapter);
            String key = filter.key;
            ArrayList<String> values = new ArrayList<>(filter.values.keySet());
            ArrayList<String> keys = new ArrayList<>(filter.values.values());
            filterKVAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                View pre = null;

                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (sortData.filterSelect.get(key) == null || !sortData.filterSelect.get(key).equals(values.get(position))) {
                        sortData.filterSelect.put(key, keys.get(position));
                        selectChange = true;
                        if (pre != null) {
                            TextView val = pre.findViewById(R.id.filterValue);
                            val.getPaint().setFakeBoldText(false);
                            val.setTextColor(getContext().getResources().getColor(R.color.color_FFFFFF));
                        }
                        TextView val = view.findViewById(R.id.filterValue);
                        val.getPaint().setFakeBoldText(true);
                        val.setTextColor(getContext().getResources().getColor(R.color.color_02F8E1));
                        pre = view;
                    }
                }
            });
            filterKVAdapter.setNewData(values);
            filterRoot.addView(line);
        }
    }

    private boolean selectChange = false;

    public void show() {
        selectChange = false;
        showPopupWindow();
    }
}