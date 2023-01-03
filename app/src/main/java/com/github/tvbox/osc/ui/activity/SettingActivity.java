package com.github.tvbox.osc.ui.activity;

import android.os.Build;
import android.os.Handler;
import android.view.KeyEvent;

import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.ui.adapter.SettingPageAdapter;
import com.github.tvbox.osc.ui.fragment.ModelSettingFragment;
import com.github.tvbox.osc.util.AppManager;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.urlhttp.JumpUtils;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class SettingActivity extends BaseActivity {
//    private TvRecyclerView mGridView;
    private ViewPager mViewPager;
    //    private SettingMenuAdapter sortAdapter;
    private SettingPageAdapter pageAdapter;
    private List<BaseLazyFragment> fragments = new ArrayList<>();
    private boolean sortChange = false;
    private int defaultSelected = 0;
    private int sortFocused = 0;
    private Handler mHandler = new Handler();
    private String homeSourceKey;
    private String currentApi;
    private int homeRec;
    private int dnsOpt;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_setting;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void init() {
        initView();
        initData();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
//        getWindow().addOnFrameMetricsAvailableListener(new Window.OnFrameMetricsAvailableListener() {
//            @Override
//            public void onFrameMetricsAvailable(Window window, FrameMetrics frameMetrics, int dropCountSinceLastInvocation) {
//                long layoutMeasureDurationNs =
//                        frameMetrics.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION);
//                long DRAWDurationNs =
//                        frameMetrics.getMetric(FrameMetrics.DRAW_DURATION);
//
//                LogUtils.d("gaozhongkui", "测量耗时：" + layoutMeasureDurationNs/10000 + "  绘制耗时：" + DRAWDurationNs/10000);
//            }
//        }, new Handler());
    }

    private void initView() {
//        mGridView = findViewById(R.id.mGridView);
        mViewPager = findViewById(R.id.mViewPager);
//        sortAdapter = new SettingMenuAdapter();
//        mGridView.setAdapter(sortAdapter);
//        mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
//        sortAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
//            @Override
//            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
//                if (view.getId() == R.id.tvName) {
//                    if (view.getParent() != null) {
//                        ((ViewGroup) view.getParent()).requestFocus();
//                        sortFocused = position;
//                        if (sortFocused != defaultSelected) {
//                            defaultSelected = sortFocused;
//                            mViewPager.setCurrentItem(sortFocused, false);
//                        }
//                    }
//                }
//            }
//        });
//        mGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
//            @Override
//            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
//                if (itemView != null) {
//                    TextView tvName = itemView.findViewById(R.id.tvName);
//                    tvName.setTextColor(getResources().getColor(R.color.color_CCFFFFFF));
//                }
//            }
//
//            @Override
//            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
//                if (itemView != null) {
//                    sortChange = true;
//                    sortFocused = position;
//                    TextView tvName = itemView.findViewById(R.id.tvName);
//                    tvName.setTextColor(Color.WHITE);
//                }
//            }
//
//            @Override
//            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
//
//            }
//        });
    }

    private void initData() {
        currentApi = Hawk.get(HawkConfig.API_URL, "");
        homeSourceKey = ApiConfig.get().getHomeSourceBean().getKey();
        homeRec = Hawk.get(HawkConfig.HOME_REC, 0);
        dnsOpt = Hawk.get(HawkConfig.DOH_URL, 0);
//        List<String> sortList = new ArrayList<>();
//        sortList.add("设置其他");
//        sortAdapter.setNewData(sortList);
        initViewPager();
    }

    private void initViewPager() {
        fragments.add(ModelSettingFragment.newInstance());
        pageAdapter = new SettingPageAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setCurrentItem(0);
    }

    private Runnable mDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (sortChange) {
                sortChange = false;
                if (sortFocused != defaultSelected) {
                    defaultSelected = sortFocused;
                    mViewPager.setCurrentItem(sortFocused, false);
                }
            }
        }
    };

    private Runnable mDevModeRun = new Runnable() {
        @Override
        public void run() {
            devMode = "";
        }
    };


    public interface DevModeCallback {
        void onChange();
    }

    public static DevModeCallback callback = null;

    String devMode = "";

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            mHandler.removeCallbacks(mDataRunnable);
            int keyCode = event.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_0:
                    mHandler.removeCallbacks(mDevModeRun);
                    devMode += "0";
                    mHandler.postDelayed(mDevModeRun, 200);
                    if (devMode.length() >= 4) {
                        if (callback != null) {
                            callback.onChange();
                        }
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            mHandler.postDelayed(mDataRunnable, 200);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if ((homeSourceKey != null && !homeSourceKey.equals(Hawk.get(HawkConfig.HOME_API, ""))) ||
                !currentApi.equals(Hawk.get(HawkConfig.API_URL, "")) ||
                homeRec != Hawk.get(HawkConfig.HOME_REC, 0) ||
                dnsOpt != Hawk.get(HawkConfig.DOH_URL, 0)) {
            AppManager.getInstance().finishAllActivity();
            JumpUtils.forceRestartHomeActivity(this);
            ToastUtils.showShort("正在更新首页");
        } else {
            super.onBackPressed();
        }
    }
}