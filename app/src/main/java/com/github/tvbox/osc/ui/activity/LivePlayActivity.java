package com.github.tvbox.osc.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.Epginfo;
import com.github.tvbox.osc.bean.LiveChannelGroup;
import com.github.tvbox.osc.bean.LiveChannelItem;
import com.github.tvbox.osc.bean.LiveDayListGroup;
import com.github.tvbox.osc.bean.LiveEpgDate;
import com.github.tvbox.osc.bean.LivePlayerManager;
import com.github.tvbox.osc.bean.LiveSettingGroup;
import com.github.tvbox.osc.bean.LiveSettingItem;
import com.github.tvbox.osc.player.controller.LiveController;
import com.github.tvbox.osc.ui.adapter.LiveChannelGroupAdapter;
import com.github.tvbox.osc.ui.adapter.LiveChannelItemAdapter;
import com.github.tvbox.osc.ui.adapter.LiveEpgAdapter;
import com.github.tvbox.osc.ui.adapter.LiveEpgDateAdapter;
import com.github.tvbox.osc.ui.adapter.LiveSettingGroupAdapter;
import com.github.tvbox.osc.ui.adapter.LiveSettingItemAdapter;
import com.github.tvbox.osc.ui.dialog.LivePasswordDialog;
import com.github.tvbox.osc.ui.dialog.LiveStoreDialog;
import com.github.tvbox.osc.ui.tv.widget.ViewObj;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.Force;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.LiveFloatViewUtil;
import com.github.tvbox.osc.util.M3uLiveParser;
import com.github.tvbox.osc.util.live.TxtSubscribe;
import com.github.tvbox.osc.util.urlhttp.CallBackUtil;
import com.github.tvbox.osc.util.urlhttp.UrlHttpUtil;
import com.github.tvbox.osc.viewmodel.LiveViewModel;
import com.google.gson.JsonArray;
import com.lzf.easyfloat.EasyFloat;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

import me.jessyan.autosize.utils.AutoSizeUtils;
import xyz.doikki.videoplayer.player.VideoView;

/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LivePlayActivity extends BaseActivity {
    private VideoView mVideoView;
    //    private TextView tvChannelInfo;
    private TextView tvTime;
    private TextView tvNetSpeed;
    private View tvLeftChannelListLayout;
    private TvRecyclerView mChannelGroupView;
    private TvRecyclerView mLiveChannelView;
    private LiveChannelGroupAdapter liveChannelGroupAdapter;
    private LiveChannelItemAdapter liveChannelItemAdapter = new LiveChannelItemAdapter();

    private LiveViewModel mLiveViewModel;

    private LinearLayout tvRightSettingLayout;
    private TvRecyclerView mSettingGroupView;
    private TvRecyclerView mSettingItemView;
    private LiveSettingGroupAdapter liveSettingGroupAdapter;
    private LiveSettingItemAdapter liveSettingItemAdapter;
    private List<LiveSettingGroup> liveSettingGroupList = new ArrayList<>();

    public static int currentChannelGroupIndex = 1;
    private Handler mHandler = new Handler();

    private List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();
    private int currentLiveChannelIndex = -1;
    private int currentLiveChangeSourceTimes = 0;
    private LiveChannelItem currentLiveChannelItem = null;
    private LivePlayerManager livePlayerManager = new LivePlayerManager();
    private ArrayList<Integer> channelGroupPasswordConfirmed = new ArrayList<>();
    private boolean needNotify;//在我的收藏里移除收藏时为true
    //EPG   by 龍
    private static LiveChannelItem channel_Name = null;
    private static Hashtable hsEpg = new Hashtable();
    private CountDownTimer countDownTimer;
    private CountDownTimer progressCountDownTimer;
    private int seekPosition;
    float simSlideOffset = 0;
    private boolean isLongPress;
    private View divLoadEpg;
    private View divLoadEpgleft;
    //    private LinearLayout lv_epg;
    RelativeLayout ll_epg;
    TextView tv_channelnum;
    TextView tip_chname;
    TextView tip_epg1;
    TextView tip_epg2;
    TextView tv_srcinfo;
    TextView tv_curepg_left;
    TextView tv_nextepg_left;
    public String epgStringAddress = "";

    private TvRecyclerView mEpgDateGridView;
    private TvRecyclerView mRightEpgList;
    private LiveEpgDateAdapter liveEpgDateAdapter;
    private LiveEpgAdapter epgListAdapter;

    private List<LiveDayListGroup> liveDayList = new ArrayList<>();


    //laodao 7day replay
    public static SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat formatDate1 = new SimpleDateFormat("MM-dd");
    public static String day = formatDate.format(new Date());
    public static Date nowday = new Date();

    private boolean isSHIYI = false;
    private static String shiyi_time;//时移时间
    private static int shiyi_time_c;//时移时间差值
    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
    private View backcontroller;
    private TextView tv_currentpos;
    private TextView tv_duration;
    private SeekBar sBar;
    private View iv_playpause;
    private LiveController liveController;
    private ViewGroup mPlayRoot;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_play;
    }

    @Override
    protected void init() {
        epgStringAddress = Hawk.get(HawkConfig.EPG_URL, "");
        if (epgStringAddress == null || epgStringAddress.length() < 5)
            epgStringAddress = "http://epg.aishangtv.top/live_proxy_epg_diyp.php";

        setLoadSir(findViewById(R.id.live_root));
        mVideoView = findViewById(R.id.mVideoView);
        mLiveViewModel = new ViewModelProvider(this).get(LiveViewModel.class);
        mLiveViewModel.result.observe(this, new Observer<Object>() {
            @Override
            public void onChanged(Object o) {
                if (o instanceof String) {
                    mVideoView.setUrl((String) o);
                    mVideoView.start();
                }
            }
        });

        tvLeftChannelListLayout = findViewById(R.id.tvLeftChannnelListLayout);
        mChannelGroupView = findViewById(R.id.mGroupGridView);
        mLiveChannelView = findViewById(R.id.mChannelGridView);
        tvRightSettingLayout = findViewById(R.id.tvRightSettingLayout);
        mSettingGroupView = findViewById(R.id.mSettingGroupView);
        mSettingItemView = findViewById(R.id.mSettingItemView);
        tvTime = findViewById(R.id.tvTime);
        tvNetSpeed = findViewById(R.id.tvNetSpeed);
        //EPG  findViewById  by 龍
        tip_chname = findViewById(R.id.tv_channel_bar_name);//底部名称
        tv_channelnum = findViewById(R.id.tv_channel_bottom_number); //底部数字
        tip_epg1 = findViewById(R.id.tv_current_program_time);//底部EPG当前节目信息
        tip_epg2 = findViewById(R.id.tv_next_program_time);//底部EPG当下个节目信息
        tv_srcinfo = findViewById(R.id.tv_source);//线路状态
        tv_curepg_left = findViewById(R.id.tv_current_program);//当前节目
        tv_nextepg_left = findViewById(R.id.tv_next_program);//下一节目
        ll_epg = findViewById(R.id.ll_epg);
        mPlayRoot = findViewById(R.id.live_root);
        divLoadEpg = findViewById(R.id.divLoadEpg);
        divLoadEpgleft = findViewById(R.id.divLoadEpgleft);
        //laodao 7day replay
        mEpgDateGridView = findViewById(R.id.mEpgDateGridView);
        Hawk.put(HawkConfig.NOW_DATE, formatDate.format(new Date()));
        day = formatDate.format(new Date());
        nowday = new Date();
        mRightEpgList = findViewById(R.id.lv_epg);
        sBar = findViewById(R.id.pb_progressbar);
        tv_currentpos = findViewById(R.id.tv_currentpos);
        backcontroller = findViewById(R.id.backcontroller);
        tv_duration = findViewById(R.id.tv_duration);
        iv_playpause = findViewById(R.id.iv_playpause);
        iv_playpause.setOnClickListener(arg0 -> {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
                countDownTimer.cancel();
                iv_playpause.setBackground(ContextCompat.getDrawable(LivePlayActivity.this, R.drawable.dkplayer_ic_action_play_arrow));
            } else {
                mVideoView.start();
                countDownTimer.start();
                iv_playpause.setBackground(ContextCompat.getDrawable(LivePlayActivity.this, R.drawable.dkplayer_ic_action_pause));
            }
        });
        divLoadEpgleft.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                View view = findViewById(R.id.select_bg);
                if (hasFocus) {
                    view.setSelected(true);
                    view.setFocusableInTouchMode(true);
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.clearFocus();
                    view.setVisibility(View.GONE);
                }
            }
        });
        divLoadEpg.setOnFocusChangeListener((v, hasFocus) -> {
            View view = findViewById(R.id.select_bg2);
            if (hasFocus) {
                view.setSelected(true);
                view.setFocusableInTouchMode(true);
                view.setVisibility(View.VISIBLE);
            } else {
                view.clearFocus();
                view.setVisibility(View.GONE);
            }
        });
        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromuser) {
                if (!fromuser) {
                    return;
                }
                mHandler.removeCallbacks(liveProgressRunable);
                mHandler.postDelayed(liveProgressRunable, 3000);
                if (countDownTimer != null) {
                    mVideoView.seekTo(progress);
                    countDownTimer.cancel();
                    countDownTimer.start();
                }
            }


        });
        sBar.setOnKeyListener((arg0, keycode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keycode == KeyEvent.KEYCODE_DPAD_CENTER || keycode == KeyEvent.KEYCODE_ENTER) {
                    if (mVideoView.isPlaying()) {
                        mVideoView.pause();
                        countDownTimer.cancel();
                    } else {
                        mVideoView.start();
                        countDownTimer.start();
                    }
                }
            }
            return false;
        });
        initEpgDateView();
        initEpgListView();
        initDayList();
        initVideoView();
        initChannelGroupView();
        initLiveChannelView();
        initSettingGroupView();
        initSettingItemView();
        initLiveChannelList();
        initLiveSettingGroupList();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("isFromFloat", false)) {
            currentLiveChannelItem = (LiveChannelItem) intent.getSerializableExtra("currentLiveChannelItem");
            currentChannelGroupIndex = intent.getIntExtra("currentChannelGroupIndex", 1);
            currentLiveChannelIndex = liveChannelItemAdapter.getData().indexOf(currentLiveChannelItem);
        }
    }

    //获取EPG并存储 // 百川epg  DIYP epg   51zmt epg ------- 自建EPG格式输出格式请参考 51zmt
    private List<Epginfo> epgdata = new ArrayList<>();

    private void showEpg(Date date, ArrayList<Epginfo> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            epgdata = arrayList;
            epgListAdapter.CanBack(currentLiveChannelItem.getinclude_back());
            epgListAdapter.setNewData(epgdata);

            int i = -1;
            int size = epgdata.size() - 1;
            while (size >= 0) {
                if (new Date().compareTo(((Epginfo) epgdata.get(size)).startdateTime) >= 0) {
                    break;
                }
                size--;
            }
            i = size;
            if (i >= 0 && new Date().compareTo(epgdata.get(i).enddateTime) <= 0) {
                mRightEpgList.setSelectedPosition(i);
                mRightEpgList.setSelection(i);
                epgListAdapter.setSelectedEpgIndex(i);
                int finalI = i;
                mRightEpgList.post(new Runnable() {
                    @Override
                    public void run() {
                        mRightEpgList.smoothScrollToPosition(finalI);
                    }
                });
            }
        } else {

            Epginfo epgbcinfo = new Epginfo(date, "暂无节目信息", date, "00:00", "23:59", 0);
            arrayList.add(epgbcinfo);
            epgdata = arrayList;
            epgListAdapter.setNewData(epgdata);

            //  mRightEpgList.setAdapter(epgListAdapter);
        }
    }

    public void getEpg(Date date) {
        String channelName = channel_Name.getChannelName();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        epgListAdapter.CanBack(currentLiveChannelItem.getinclude_back());
        showEpg(date, new ArrayList<>());
        showBottomEpg();
        UrlHttpUtil.get(epgStringAddress + "?ch=" + URLEncoder.encode(channelName) + "&date=" + timeFormat.format(date), new CallBackUtil.CallBackString() {
            public void onFailure(int i, String str) {
            }

            public void onResponse(String paramString) {

                ArrayList<Epginfo> arrayList = new ArrayList<>();

                try {
                    if (paramString.contains("epg_data")) {
                        final JSONArray jSONArray = new JSONObject(paramString).optJSONArray("epg_data");
                        if (jSONArray != null) for (int b = 0; b < jSONArray.length(); b++) {
                            JSONObject jSONObject = jSONArray.getJSONObject(b);
                            Epginfo epgbcinfo = new Epginfo(date, jSONObject.optString("title"), date, jSONObject.optString("start"), jSONObject.optString("end"), b);
                            arrayList.add(epgbcinfo);
                        }
                    }

                } catch (JSONException jSONException) {
                }
                showEpg(date, arrayList);
                String savedEpgKey = channelName + "_" + liveEpgDateAdapter.getItem(liveEpgDateAdapter.getSelectedIndex()).getDatePresented();
                if (!hsEpg.contains(savedEpgKey)) hsEpg.put(savedEpgKey, arrayList);
                showBottomEpg();
            }
        });
    }

    //显示底部EPG
    private void showBottomEpg() {
        if (isSHIYI) return;
        if (channel_Name.getChannelName() != null) {
            ll_epg.setVisibility(View.VISIBLE);

            tip_chname.setText(channel_Name.getChannelName());
            tv_channelnum.setText("" + channel_Name.getChannelNum());
            tip_epg1.setText("暂无信息");
            ((TextView) findViewById(R.id.tv_current_program_name)).setText("");
            tip_epg2.setText("开源测试软件,请勿商用以及播放违法内容");
            ((TextView) findViewById(R.id.tv_next_program_name)).setText("");
            String savedEpgKey = channel_Name.getChannelName() + "_" + liveEpgDateAdapter.getItem(liveEpgDateAdapter.getSelectedIndex()).getDatePresented();
            if (hsEpg.containsKey(savedEpgKey)) {
                ArrayList arrayList = (ArrayList) hsEpg.get(savedEpgKey);
                if (arrayList != null && arrayList.size() > 0) {
                    int size = arrayList.size() - 1;
                    while (size >= 0) {
                        if (new Date().compareTo(((Epginfo) arrayList.get(size)).startdateTime) >= 0) {
                            tip_epg1.setText(((Epginfo) arrayList.get(size)).start + "--" + ((Epginfo) arrayList.get(size)).end);
                            ((TextView) findViewById(R.id.tv_current_program_name)).setText(((Epginfo) arrayList.get(size)).title);
                            if (size != arrayList.size() - 1) {
                                tip_epg2.setText(((Epginfo) arrayList.get(size + 1)).start + "--" + ((Epginfo) arrayList.get(size)).end);
                                ((TextView) findViewById(R.id.tv_next_program_name)).setText(((Epginfo) arrayList.get(size + 1)).title);
                            }
                            break;
                        } else {
                            size--;
                        }
                    }
                }
                epgListAdapter.CanBack(currentLiveChannelItem.getinclude_back());
                epgListAdapter.setNewData(arrayList);
            } else {
//                int selectedIndex = liveEpgDateAdapter.getSelectedIndex();
//                if (selectedIndex < 0) getEpg(new Date());
//                else getEpg(liveEpgDateAdapter.getData().get(selectedIndex).getDateParamVal());
            }

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            countDownTimer = new CountDownTimer(5000, 1000) {//底部epg隐藏时间设定
                public void onTick(long j) {
                }

                public void onFinish() {
                    ll_epg.setVisibility(View.GONE);
                }
            };
            countDownTimer.start();
            if (channel_Name == null || channel_Name.getSourceNum() <= 0) {
                tv_srcinfo.setText("1/1");
            } else {
                tv_srcinfo.setText("[线路" + (channel_Name.getSourceIndex() + 1) + "/" + channel_Name.getSourceNum() + "]");
            }
            mHandler.post(mUpdateNetSpeedRun);
        }
    }

    //频道列表
    public void divLoadEpgRight(View view) {

        mChannelGroupView.setVisibility(View.GONE);
        mRightEpgList.setVisibility(View.VISIBLE);
        mEpgDateGridView.setVisibility(View.VISIBLE);
        divLoadEpgleft.setVisibility(View.VISIBLE);
        divLoadEpg.setVisibility(View.GONE);
        mRightEpgList.setSelectedPosition(epgListAdapter.getSelectedIndex());
        epgListAdapter.notifyItemChanged(epgListAdapter.getSelectedIndex());


    }

    //频道列表
    public void divLoadEpgLeft(View view) {
        // mRightEpgList.setVisibility(View.GONE);
        mChannelGroupView.setVisibility(View.VISIBLE);
        mRightEpgList.setVisibility(View.GONE);
        mEpgDateGridView.setVisibility(View.GONE);
        divLoadEpgleft.setVisibility(View.GONE);
        divLoadEpg.setVisibility(View.VISIBLE);
    }


    private long exitTime;

    @Override
    public void onBackPressed() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        } else if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        } else {
            if (System.currentTimeMillis() - exitTime < 2000) {
                mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                mHandler.removeCallbacks(mUpdateNetSpeedRun);
                super.onBackPressed();
            } else {
                exitTime = System.currentTimeMillis();
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).setTextSize(AutoSizeUtils.mm2px(this, 10)).show("再按一次退出直播");
            }

        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (!checkCanChangeProgress()) {
                return super.onKeyLongPress(keyCode, event);
            }
            isLongPress = true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (!checkCanChangeProgress()) {
                return super.onKeyDown(keyCode, event);
            }
            if (isLongPress) {
                int dir = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1;
                tvSlideStart(dir);
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                event.startTracking();
                return !isListOrSettingLayoutVisible();
            }
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {//菜单键
            //长按
            if ((event.getFlags() & KeyEvent.FLAG_LONG_PRESS) != 0) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    showLiveSourceDialog();
                }
            } else {
                if (event.getAction() == KeyEvent.ACTION_UP) showSettingGroup();
            }
        }
        handleKeyDownEvent(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }

    private void handleKeyDownEvent(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!isListOrSettingLayoutVisible()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false)) playNext();
                        else playPrevious();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false)) playPrevious();
                        else playNext();
                        break;
                    case KeyEvent.KEYCODE_ESCAPE:
                        onBackPressed();
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        showChannelList();
                        break;
                }
            } else {
                if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    onBackPressed();
                }
            }
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (!checkCanChangeProgress()) {//如果不能快进快退，就显示
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (!isListOrSettingLayoutVisible()) {
                        playPreSource();
                    }
                } else {
                    if (!isListOrSettingLayoutVisible()) {
                        playNextSource();
                    }
                }
                return super.onKeyUp(keyCode, event);
            }
            if (isLongPress) {
                isLongPress = false;
                mVideoView.seekTo(seekPosition);
                seekPosition = 0;
                simSlideOffset = 0;
                liveController.hideProgressContainer();
                return true;
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {//菜单键
            showSettingGroup();
        }

        return super.onKeyUp(keyCode, event);
    }

    public void tvSlideStart(int dir) {
        int duration = (int) mVideoView.getDuration();
        if (duration <= 0) return;
        // 每次5秒
        simSlideOffset += (5000 * dir);
        int currentPosition = (int) mVideoView.getCurrentPosition();
        int position = (int) (simSlideOffset + currentPosition);
        if (position > duration) position = duration;
        if (position < 0) position = 0;
        seekPosition = position;
        liveController.updateSeekUI(currentPosition, position, duration);
//        simSeekPosition = position;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            ViewGroup parent = (ViewGroup) (mVideoView.getParent());
            if (parent.getId() != R.id.live_root) {
                EasyFloat.dismiss(LiveFloatViewUtil.FLOAT_TAG);
                parent.removeView(mVideoView);
                mPlayRoot.addView(mVideoView, 0);
                mVideoView.setVideoController(liveController);
            } else {
                mVideoView.resume();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
            mVideoView = null;
        }
        mHandler.removeCallbacksAndMessages(null);
        Force.get().stop();
    }

    private void showChannelList() {
        if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        }
        if (tvLeftChannelListLayout.getVisibility() == View.INVISIBLE) {
            //重新载入上一次状态
            liveChannelItemAdapter.setNewData(getLiveChannels(currentChannelGroupIndex));
            mLiveChannelView.setSelection(currentLiveChannelIndex);
            mChannelGroupView.setSelection(currentChannelGroupIndex);
            mHandler.postDelayed(mFocusCurrentChannelAndShowChannelList, 200);
        } else {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
    }

    private final Runnable mHideChannelListRun = this::hideChanelList;
    private final Runnable mFocusCurrentChannelAndShowChannelList = new Runnable() {
        @Override
        public void run() {
            if (mChannelGroupView.isScrolling() || mLiveChannelView.isScrolling() || mChannelGroupView.isComputingLayout() || mLiveChannelView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                liveChannelGroupAdapter.setSelectedGroupIndex(currentChannelGroupIndex);
                liveChannelItemAdapter.setSelectedChannelIndex(currentLiveChannelIndex);
                RecyclerView.ViewHolder holder = mLiveChannelView.findViewHolderForAdapterPosition(currentLiveChannelIndex);
                if (holder != null) holder.itemView.requestFocus();
                tvLeftChannelListLayout.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvLeftChannelListLayout.getLayoutParams();
                ViewObj viewObj = new ViewObj(tvLeftChannelListLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -AutoSizeUtils.mm2px(mContext, 570), 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mHandler.removeCallbacks(mHideChannelListRun);
                        mHandler.postDelayed(mHideChannelListRun, 5000);
                    }
                });
                animator.start();
            }
        }
    };
    private Runnable mHideSettingLayoutRun = () -> hideSettingLayout();

    private boolean playChannel(int channelGroupIndex, int liveChannelIndex, boolean changeSource) {
        if ((channelGroupIndex == currentChannelGroupIndex && liveChannelIndex == currentLiveChannelIndex && !changeSource) || (changeSource && currentLiveChannelItem.getSourceNum() == 1)) {
            // showChannelInfo();
            return true;
        }
        if (mVideoView != null) {
            mVideoView.release();
        }
        if (!changeSource) {
            currentChannelGroupIndex = channelGroupIndex;
            currentLiveChannelIndex = liveChannelIndex;
            currentLiveChannelItem = getLiveChannels(currentChannelGroupIndex).get(currentLiveChannelIndex);
            Hawk.put(HawkConfig.LIVE_CHANNEL, currentLiveChannelItem.getChannelName());
            livePlayerManager.getLiveChannelPlayer(mVideoView, currentLiveChannelItem.getChannelName());
        }

        channel_Name = currentLiveChannelItem;
        isSHIYI = false;
        if (currentLiveChannelItem.getUrl().contains("PLTV/8888")) {
            currentLiveChannelItem.setinclude_back(true);
        } else {
            currentLiveChannelItem.setinclude_back(false);
        }
        getEpg(new Date());
        mLiveViewModel.getUrl(currentLiveChannelItem);
//        mVideoView.setUrl(currentLiveChannelItem.getUrl());
//        mVideoView.start();
        return true;
    }

    private void playNext() {
        if (!isCurrentLiveChannelValid()) return;
        Integer[] groupChannelIndex = getNextChannel(1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    private void playPrevious() {
        if (!isCurrentLiveChannelValid()) return;
        Integer[] groupChannelIndex = getNextChannel(-1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    public void playPreSource() {
        if (!isCurrentLiveChannelValid()) return;
        currentLiveChannelItem.preSource();
        playChannel(currentChannelGroupIndex, currentLiveChannelIndex, true);
    }

    public void playNextSource() {
        if (!isCurrentLiveChannelValid()) return;
        currentLiveChannelItem.nextSource();
        playChannel(currentChannelGroupIndex, currentLiveChannelIndex, true);
    }

    private boolean checkCanChangeProgress() {//大于1分钟才支持快进
        return mVideoView != null && mVideoView.getDuration() > 60 * 1000;
    }

    //显示设置列表
    private void showSettingGroup() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
        if (tvRightSettingLayout.getVisibility() == View.INVISIBLE) {
//            if (!isCurrentLiveChannelValid()) return;
            //重新载入默认状态
            loadCurrentSourceList();
            liveSettingGroupAdapter.setNewData(liveSettingGroupList);
            selectSettingGroup(0, false);
            mSettingGroupView.scrollToPosition(0);
            if (currentLiveChannelItem != null) {
                mSettingItemView.scrollToPosition(currentLiveChannelItem.getSourceIndex());
            }
            mHandler.postDelayed(mFocusAndShowSettingGroup, 200);
        } else {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        }
    }

    private Runnable mFocusAndShowSettingGroup = new Runnable() {
        @Override
        public void run() {
            if (mSettingGroupView.isScrolling() || mSettingItemView.isScrolling() || mSettingGroupView.isComputingLayout() || mSettingItemView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mSettingGroupView.findViewHolderForAdapterPosition(0);
                if (holder != null) holder.itemView.requestFocus();
                tvRightSettingLayout.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
                if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
                    ViewObj viewObj = new ViewObj(tvRightSettingLayout, params);
                    ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), -tvRightSettingLayout.getLayoutParams().width, 0);
                    animator.setDuration(200);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mHandler.postDelayed(mHideSettingLayoutRun, 5000);
                        }
                    });
                    animator.start();
                }
            }
        }
    };

    private void hideChanelList() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvLeftChannelListLayout.getLayoutParams();
            ViewObj viewObj = new ViewObj(tvLeftChannelListLayout, params);
            ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), 0, -AutoSizeUtils.mm2px(mContext, 570));
            animator.setDuration(200);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    tvLeftChannelListLayout.setVisibility(View.INVISIBLE);
                }
            });
            animator.start();
        }
    }

    private void hideSettingLayout() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
        if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            ViewObj viewObj = new ViewObj(tvRightSettingLayout, params);
            ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), 0, -tvRightSettingLayout.getLayoutParams().width);
            animator.setDuration(200);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    tvRightSettingLayout.setVisibility(View.INVISIBLE);
                    liveSettingGroupAdapter.setSelectedGroupIndex(-1);
                }
            });
            animator.start();
        }
    }

    //laodao 7天Epg数据绑定和展示
    private void initEpgListView() {
        mRightEpgList.setHasFixedSize(true);
        mRightEpgList.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        epgListAdapter = new LiveEpgAdapter();
        mRightEpgList.setAdapter(epgListAdapter);

        mRightEpgList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });
        //电视
        mRightEpgList.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                epgListAdapter.setFocusedEpgIndex(-1);
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                epgListAdapter.setFocusedEpgIndex(position);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

                Date date = liveEpgDateAdapter.getSelectedIndex() < 0 ? new Date() : liveEpgDateAdapter.getData().get(liveEpgDateAdapter.getSelectedIndex()).getDateParamVal();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                Epginfo selectedData = epgListAdapter.getItem(position);
                if (selectedData == null) {
                    return;
                }
                String targetDate = dateFormat.format(date);
                String shiyiStartdate = targetDate + selectedData.originStart.replace(":", "") + "30";
                String shiyiEnddate = targetDate + selectedData.originEnd.replace(":", "") + "30";
                Date now = new Date();
                if (now.compareTo(selectedData.startdateTime) < 0) {
                    return;
                }
                epgListAdapter.setSelectedEpgIndex(position);
                if (now.compareTo(selectedData.startdateTime) >= 0 && now.compareTo(selectedData.enddateTime) <= 0) {
                    mVideoView.release();
                    isSHIYI = false;
                    mLiveViewModel.getUrl(currentLiveChannelItem);
//                    mVideoView.setUrl(currentLiveChannelItem.getUrl());
//                    mVideoView.start();
                    epgListAdapter.setShiyiSelection(-1, false, timeFormat.format(date));
                    return;
                }
                String shiyiUrl = currentLiveChannelItem.getUrl();
                if (now.compareTo(selectedData.startdateTime) < 0) {

                } else if (shiyiUrl.contains("PLTV/8888")) {

                    mHandler.removeCallbacks(mHideChannelListRun);
                    mHandler.postDelayed(mHideChannelListRun, 100);
                    mVideoView.release();
                    shiyi_time = shiyiStartdate + "-" + shiyiEnddate;
                    isSHIYI = true;
                    //mCanSeek=true;
                    if (shiyiUrl.contains("/PLTV/")) {
                        if (shiyiUrl.indexOf("?") <= 0) {
                            shiyiUrl = shiyiUrl.replaceAll("/PLTV/", "/TVOD/");
                            shiyiUrl += "?playseek=" + shiyi_time;
                        } else if (shiyiUrl.indexOf("playseek") > 0) {
                            shiyiUrl = shiyiUrl.replaceAll("playseek=(.*)", "playseek=" + shiyi_time);
                        } else {
                            shiyiUrl += "&playseek=" + shiyi_time;
                        }
                    }
                    mLiveViewModel.getUrl(shiyiUrl);
//                    mVideoView.setUrl(shiyiUrl);
//                    mVideoView.start();
                    epgListAdapter.setShiyiSelection(position, true, timeFormat.format(date));
//                    epgListAdapter.notifyDataSetChanged();
                    mRightEpgList.setSelectedPosition(position);
                    mRightEpgList.post(new Runnable() {
                        @Override
                        public void run() {
                            mRightEpgList.smoothScrollToPosition(position);
                        }
                    });
                    shiyi_time_c = (int) getTime(formatDate.format(nowday) + " " + selectedData.start + ":" + "30", formatDate.format(nowday) + " " + selectedData.end + ":" + "30");
                    sBar.setMax(shiyi_time_c * 1000);
                    sBar.setProgress((int) mVideoView.getCurrentPosition());
                    tv_currentpos.setText(durationToString((int) mVideoView.getCurrentPosition()));
                    tv_duration.setText(durationToString(shiyi_time_c * 1000));
                    showProgressBars(true);
                }
            }
        });

        //手机/模拟器
        epgListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Date date = liveEpgDateAdapter.getSelectedIndex() < 0 ? new Date() : liveEpgDateAdapter.getData().get(liveEpgDateAdapter.getSelectedIndex()).getDateParamVal();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                Epginfo selectedData = epgListAdapter.getItem(position);
                String targetDate = dateFormat.format(date);
                String shiyiStartdate = targetDate + selectedData.originStart.replace(":", "") + "30";
                String shiyiEnddate = targetDate + selectedData.originEnd.replace(":", "") + "30";
                Date now = new Date();
                if (now.compareTo(selectedData.startdateTime) < 0) {
                    return;
                }
                epgListAdapter.setSelectedEpgIndex(position);
                if (now.compareTo(selectedData.startdateTime) >= 0 && now.compareTo(selectedData.enddateTime) <= 0) {
                    mVideoView.release();
                    isSHIYI = false;
                    mLiveViewModel.getUrl(currentLiveChannelItem);
//                    mVideoView.setUrl(currentLiveChannelItem.getUrl());
//                    mVideoView.start();
                    epgListAdapter.setShiyiSelection(-1, false, timeFormat.format(date));
                    return;
                }
                String shiyiUrl = currentLiveChannelItem.getUrl();
                if (now.compareTo(selectedData.startdateTime) < 0) {

                } else if (shiyiUrl.contains("PLTV/8888")) {
                    mHandler.removeCallbacks(mHideChannelListRun);
                    mHandler.postDelayed(mHideChannelListRun, 100);

                    mVideoView.release();
                    shiyi_time = shiyiStartdate + "-" + shiyiEnddate;
                    isSHIYI = true;
                    //mCanSeek=true;
                    if (shiyiUrl.contains("/PLTV/")) {
                        if (shiyiUrl.indexOf("?") <= 0) {
                            shiyiUrl = shiyiUrl.replaceAll("/PLTV/", "/TVOD/");
                            shiyiUrl += "?playseek=" + shiyi_time;
                        } else if (shiyiUrl.indexOf("playseek") > 0) {
                            shiyiUrl = shiyiUrl.replaceAll("playseek=(.*)", "playseek=" + shiyi_time);
                        } else {
                            shiyiUrl += "&playseek=" + shiyi_time;
                        }
                    }
//                    mVideoView.get(shiyiUrl);
                    mLiveViewModel.getUrl(shiyiUrl);
//                    mVideoView.setUrl(shiyiUrl);
//                    mVideoView.start();
                    epgListAdapter.setShiyiSelection(position, true, timeFormat.format(date));
                    mRightEpgList.setSelectedPosition(position);
                    mRightEpgList.post(new Runnable() {
                        @Override
                        public void run() {
                            mRightEpgList.smoothScrollToPosition(position);
                        }
                    });
                    shiyi_time_c = (int) getTime(formatDate.format(nowday) + " " + selectedData.start + ":" + "30", formatDate.format(nowday) + " " + selectedData.end + ":" + "30");
                    sBar.setMax(shiyi_time_c * 1000);
                    sBar.setProgress((int) mVideoView.getCurrentPosition());
                    tv_currentpos.setText(durationToString((int) mVideoView.getCurrentPosition()));
                    tv_duration.setText(durationToString(shiyi_time_c * 1000));
                }
            }
        });
    }

    //laoda 生成7天回放日期列表数据
    private void initDayList() {
        liveDayList.clear();
        Date firstday = new Date(nowday.getTime() - 6 * 24 * 60 * 60 * 1000);
        for (int i = 0; i < 8; i++) {
            LiveDayListGroup daylist = new LiveDayListGroup();
            Date newday = new Date(firstday.getTime() + i * 24 * 60 * 60 * 1000);
            String day = formatDate1.format(newday);
            daylist.setGroupIndex(i);
            daylist.setGroupName(day);
            liveDayList.add(daylist);
        }


    }

    //kens 7天回放数据绑定和展示
    private void initEpgDateView() {
        mEpgDateGridView.setHasFixedSize(true);
        mEpgDateGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        liveEpgDateAdapter = new LiveEpgDateAdapter();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        SimpleDateFormat datePresentFormat = new SimpleDateFormat("MM-dd");
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        for (int i = 0; i < 8; i++) {
            Date dateIns = calendar.getTime();
            LiveEpgDate epgDate = new LiveEpgDate();
            epgDate.setIndex(i);
            epgDate.setDatePresented(datePresentFormat.format(dateIns));
            epgDate.setDateParamVal(dateIns);
            liveEpgDateAdapter.addData(0, epgDate);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        mEpgDateGridView.setAdapter(liveEpgDateAdapter);
        mEpgDateGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
        mEpgDateGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                liveEpgDateAdapter.setFocusedIndex(-1);
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                liveEpgDateAdapter.setFocusedIndex(position);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                liveEpgDateAdapter.setSelectedIndex(position);
                getEpg(liveEpgDateAdapter.getData().get(position).getDateParamVal());
            }
        });

        //手机/模拟器
        liveEpgDateAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
                liveEpgDateAdapter.setSelectedIndex(position);
                getEpg(liveEpgDateAdapter.getData().get(position).getDateParamVal());
            }
        });
        liveEpgDateAdapter.setSelectedIndex(1);
    }


    private void initVideoView() {
        liveController = new LiveController(this);
        liveController.setListener(new LiveController.LiveControlListener() {
            @Override
            public boolean singleTap(MotionEvent e) {
                if (e.getX() > ScreenUtils.getScreenWidth() / 2f) {
                    if (isChanelListVisible()) {
                        hideChanelList();
                    } else if (isSettingLayoutVisible()) {
                        hideSettingLayout();
                    } else {
                        showSettingGroup();
                    }
                } else {
                    if (isChanelListVisible()) {
                        hideChanelList();
                    } else if (isSettingLayoutVisible()) {
                        hideSettingLayout();
                    } else {
                        showChannelList();
                    }

                }
                return true;
            }

            @Override
            public void longPress() {
                showProgressBars(true);
            }

            @Override
            public void playStateChanged(int playState) {
                switch (playState) {
                    case VideoView.STATE_IDLE:
                    case VideoView.STATE_PAUSED:
                        break;
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_BUFFERED:
                    case VideoView.STATE_PLAYING:
                        currentLiveChangeSourceTimes = 0;
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        sBar.setMax((int) mVideoView.getDuration());
                        tv_currentpos.setText(durationToString((int) mVideoView.getCurrentPosition()));
                        tv_duration.setText(durationToString((int) mVideoView.getDuration()));
                        liveController.hideProgressContainer();
                        break;
                    case VideoView.STATE_ERROR:
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.post(mConnectTimeoutChangeSourceRun);
                        break;
                    case VideoView.STATE_PREPARING:
                    case VideoView.STATE_BUFFERING:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.postDelayed(mConnectTimeoutChangeSourceRun, (Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 5) + 1) * 5000);
                        break;
                }
            }

            @Override
            public void changeSource(int direction) {
                if (checkCanChangeProgress()) {//如果视频支持快进，走快进逻辑
                    return;
                }
                if (direction > 0) playNextSource();
                else playPreSource();
            }

            @Override
            public void nextChanel() {

            }

            @Override
            public void preChanel() {

            }
        });
        liveController.setCanChangePosition(true);
        liveController.setEnableInNormal(true);
        liveController.setGestureEnabled(true);
        liveController.setDoubleTapTogglePlayEnabled(false);
        liveController.setOnDoubleTapListener(() -> {
            showProgressBars(true);

        });
        mVideoView.setVideoController(liveController);
        mVideoView.setProgressManager(null);
    }

    private Runnable mConnectTimeoutChangeSourceRun = new Runnable() {
        @Override
        public void run() {
            currentLiveChangeSourceTimes++;
            if (currentLiveChannelItem.getSourceNum() == currentLiveChangeSourceTimes) {
                currentLiveChangeSourceTimes = 0;
                Integer[] groupChannelIndex = getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
                playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
            } else {
                playNextSource();
            }
        }
    };


    private void initChannelGroupView() {
        mChannelGroupView.setHasFixedSize(true);
        mChannelGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveChannelGroupAdapter = new LiveChannelGroupAdapter();
        mChannelGroupView.setAdapter(liveChannelGroupAdapter);
        mChannelGroupView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
        mChannelGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {

            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectChannelGroup(position, true, -1);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                if (isNeedInputPassword(position)) {
                    showPasswordDialog(position, -1);
                }
            }
        });

        //手机/模拟器
        liveChannelGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectChannelGroup(position, false, -1);
            }
        });
    }

    private void checkNeedNotify(int position) {
        LiveChannelGroup liveChannelGroup = liveChannelGroupAdapter.getData().get(position);
        needNotify = liveChannelGroup.isCollected;
    }

    private void selectChannelGroup(int groupIndex, boolean focus, int liveChannelIndex) {
        checkNeedNotify(groupIndex);
        if (focus) {
            liveChannelGroupAdapter.setFocusedGroupIndex(groupIndex);
            liveChannelItemAdapter.setFocusedChannelIndex(-1);
        }
        if ((groupIndex > -1 && groupIndex != liveChannelGroupAdapter.getSelectedGroupIndex()) || isNeedInputPassword(groupIndex)) {
            liveChannelGroupAdapter.setSelectedGroupIndex(groupIndex);
            if (isNeedInputPassword(groupIndex)) {
                showPasswordDialog(groupIndex, liveChannelIndex);
                return;
            }
            loadChannelGroupDataAndPlay(groupIndex, liveChannelIndex);
        }
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, 5000);
        }
    }

    private void initLiveChannelView() {
        mLiveChannelView.setHasFixedSize(true);
        mLiveChannelView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveChannelItemAdapter = new LiveChannelItemAdapter();
        mLiveChannelView.setAdapter(liveChannelItemAdapter);
        mLiveChannelView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
        mLiveChannelView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                liveChannelGroupAdapter.setFocusedGroupIndex(-1);
                liveChannelItemAdapter.setFocusedChannelIndex(position);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                clickLiveChannel(position);
            }
        });

        //手机/模拟器
        liveChannelItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                clickLiveChannel(position);
            }
        });

        liveChannelItemAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                LiveChannelItem item = liveChannelItemAdapter.getItem(position);
                if (item == null) {
                    return true;
                }
                item.isCollected = !item.isCollected;
                LiveChannelGroup liveChannelGroupTmp = liveChannelGroupList.get(0);
                List<LiveChannelItem> tmpChanelData = liveChannelGroupTmp.getLiveChannels();
                if (item.isCollected) {//如果收藏了
                    LiveChannelItem cloneItem = item.clone();
                    liveChannelGroupTmp.getLiveChannels().add(cloneItem);
                    int index = tmpChanelData.size() - 1;
                    if (index < 0) {
                        index = 0;
                    }
                    cloneItem.setChannelIndex(index);//重制index
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).setTextSize(AutoSizeUtils.mm2px(mContext, 10)).show("收藏成功");
                } else {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).setTextSize(AutoSizeUtils.mm2px(mContext, 10)).show("收藏移除");
                    tmpChanelData.remove(item);
                    for (int i = 0; i < tmpChanelData.size(); i++) {
                        tmpChanelData.get(i).setChannelIndex(i);
                    }
                    if (needNotify) {
                        liveChannelItemAdapter.notifyItemRemoved(position);
                        liveChannelItemAdapter.notifyItemRangeChanged(position, tmpChanelData.size() - position);
                        liveChannelItemAdapter.notifyItemChanged(position);
                    }
                }
                Hawk.put(HawkConfig.LIVE_CHANELE_COLLECTD, liveChannelGroupTmp);
                return true;
            }
        });

    }

    private void clickLiveChannel(int position) {
        liveChannelItemAdapter.setSelectedChannelIndex(position);
        playChannel(liveChannelGroupAdapter.getSelectedGroupIndex(), position, false);
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, 5000);
        }
    }

    private void initSettingGroupView() {
        mSettingGroupView.setHasFixedSize(true);
        mSettingGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveSettingGroupAdapter = new LiveSettingGroupAdapter();
        mSettingGroupView.setAdapter(liveSettingGroupAdapter);
        mSettingGroupView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }
        });

        //电视
        mSettingGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectSettingGroup(position, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
            }
        });

        //手机/模拟器
        liveSettingGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectSettingGroup(position, false);
            }
        });
    }


    private void selectSettingGroup(int position, boolean focus) {
//        if (!isCurrentLiveChannelValid()) return;
        if (focus) {
            liveSettingGroupAdapter.setFocusedGroupIndex(position);
            liveSettingItemAdapter.setFocusedItemIndex(-1);
        }
        if (position == 5) {
            showLiveSourceDialog();
        }
        if (position == 6) {
            new LiveFloatViewUtil().openFloat(mVideoView, currentLiveChannelItem, (ArrayList<LiveChannelGroup>) liveChannelGroupList, currentChannelGroupIndex);
        }
        if (position == 7) {
            onBackPressed();
            finish();
        }
        if (position == liveSettingGroupAdapter.getSelectedGroupIndex() || position < -1) return;

        liveSettingGroupAdapter.setSelectedGroupIndex(position);
        liveSettingItemAdapter.setNewData(liveSettingGroupList.get(position).getLiveSettingItems());

        switch (position) {
            case 0:
                if (currentLiveChannelItem != null) {
                    liveSettingItemAdapter.selectItem(currentLiveChannelItem.getSourceIndex(), true, false);
                }
                break;
            case 1:
                liveSettingItemAdapter.selectItem(livePlayerManager.getLivePlayerScale(), true, true);
                break;
            case 2:
                liveSettingItemAdapter.selectItem(livePlayerManager.getLivePlayerType(), true, true);
                break;
        }
        int scrollToPosition = liveSettingItemAdapter.getSelectedItemIndex();
        if (scrollToPosition < 0) scrollToPosition = 0;
        mSettingItemView.scrollToPosition(scrollToPosition);
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
    }

    private void showLiveSourceDialog() {
        new LiveStoreDialog(this).show();
    }

    private void initSettingItemView() {
        mSettingItemView.setHasFixedSize(true);
        mSettingItemView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveSettingItemAdapter = new LiveSettingItemAdapter();
        mSettingItemView.setAdapter(liveSettingItemAdapter);
        mSettingItemView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }
        });

        //电视
        mSettingItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                liveSettingGroupAdapter.setFocusedGroupIndex(-1);
                liveSettingItemAdapter.setFocusedItemIndex(position);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                clickSettingItem(position);
            }
        });

        //手机/模拟器
        liveSettingItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                clickSettingItem(position);
            }
        });
    }

    private void clickSettingItem(int position) {
        int settingGroupIndex = liveSettingGroupAdapter.getSelectedGroupIndex();
        if (settingGroupIndex < 4) {
            if (position == liveSettingItemAdapter.getSelectedItemIndex()) return;
            liveSettingItemAdapter.selectItem(position, true, true);
        }
        switch (settingGroupIndex) {
            case 0://线路切换
                currentLiveChannelItem.setSourceIndex(position);
                playChannel(currentChannelGroupIndex, currentLiveChannelIndex, true);
                break;
            case 1://画面比例
                livePlayerManager.changeLivePlayerScale(mVideoView, position, currentLiveChannelItem.getChannelName());
                break;
            case 2://播放解码
                mVideoView.release();
                livePlayerManager.changeLivePlayerType(mVideoView, position, currentLiveChannelItem.getChannelName());
                mLiveViewModel.getUrl(currentLiveChannelItem);
//                mVideoView.setUrl(currentLiveChannelItem.getUrl());
//                mVideoView.start();
                break;
            case 3://超时换源
                Hawk.put(HawkConfig.LIVE_CONNECT_TIMEOUT, position);
                break;
            case 4://超时换源
                boolean select = false;
                switch (position) {
                    case 0:
                        select = !Hawk.get(HawkConfig.LIVE_SHOW_TIME, false);
                        Hawk.put(HawkConfig.LIVE_SHOW_TIME, select);
                        showTime();
                        break;
                    case 1:
                        select = !Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false);
                        Hawk.put(HawkConfig.LIVE_SHOW_NET_SPEED, select);
                        showNetSpeed();
                        break;
                    case 2:
                        select = !Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false);
                        Hawk.put(HawkConfig.LIVE_CHANNEL_REVERSE, select);
                        break;
                    case 3:
                        select = !Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false);
                        Hawk.put(HawkConfig.LIVE_CROSS_GROUP, select);
                        break;
                }
                liveSettingItemAdapter.selectItem(position, select, false);
                break;
        }
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
    }

    private void initLiveChannelList() {
        List<LiveChannelGroup> list = ApiConfig.get().getChannelGroupList();
        if (list.isEmpty()) {//在没有配置任何数据的时候，我只配置了直播列表，尝试读取本地直播列表
            ApiConfig.get().loadLiveSourceUrl("", null);
        }
        list = ApiConfig.get().getChannelGroupList();
        if (list.isEmpty()) {
            Toast.makeText(App.getInstance(), "频道列表为空", Toast.LENGTH_SHORT).show();
            showLiveSourceDialog();
            return;
        }

        if (list.size() == 1 && list.get(0).getGroupName().startsWith("http://127.0.0.1")) {
            loadProxyLives(list.get(0).getGroupName());
        } else {
            liveChannelGroupList.clear();
            liveChannelGroupList.addAll(list);
            showSuccess();
            initLiveState();
        }
    }

    private LiveChannelGroup getLiveCollected() {
        return Hawk.get(HawkConfig.LIVE_CHANELE_COLLECTD, new LiveChannelGroup());
    }

    public void loadProxyLives(String url) {
        try {
            Uri parsedUrl = Uri.parse(url);
            url = new String(Base64.decode(parsedUrl.getQueryParameter("ext"), Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP), "UTF-8");
        } catch (Throwable th) {
            Toast.makeText(App.getInstance(), "频道列表为空", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading();
        OkGo.<String>get(url).cacheKey(url).
                cacheMode(CacheMode.IF_NONE_CACHE_REQUEST).cacheTime(3 * 24 * 60 * 60 * 1000).execute(new StringCallback() {
                    @Override
                    public void onError(Response<String> response) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("直播地址加载失败" + response.getException().getMessage());
                        showLiveSourceDialog();
                        showSuccess();
                        super.onError(response);
                    }

                    @Override
                    public void onCacheSuccess(Response<String> response) {
                        super.onCacheSuccess(response);
                        inflateData(response);
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        inflateData(response);
                    }
                });
    }

    private void inflateData(Response<String> response) {
        JsonArray livesArray;
        LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> linkedHashMap = new LinkedHashMap<>();
        List<LiveChannelGroup> tmpList = M3uLiveParser.start(response.body());//尝试m3u解析
        if (tmpList.isEmpty()) {//m3u解析是空
            TxtSubscribe.parse(linkedHashMap, response.body());
            livesArray = TxtSubscribe.live2JsonArray(linkedHashMap);
            ApiConfig.get().loadLives(livesArray);
            tmpList = ApiConfig.get().getChannelGroupList();
        }
        if (tmpList.isEmpty()) {
            Toast.makeText(App.getInstance(), "频道列表为空", Toast.LENGTH_SHORT).show();
            showLiveSourceDialog();
            return;
        }
        liveChannelGroupList.clear();
        liveChannelGroupList.addAll(tmpList);
        mHandler.post(() -> {
            LivePlayActivity.this.showSuccess();
            initLiveState();
        });
    }

    private void initLiveState() {
        String lastChannelName = Hawk.get(HawkConfig.LIVE_CHANNEL, "");
        int lastChannelGroupIndex = -1;
        int lastLiveChannelIndex = -1;
        for (LiveChannelGroup liveChannelGroup : liveChannelGroupList) {
            for (LiveChannelItem liveChannelItem : liveChannelGroup.getLiveChannels()) {
                if (liveChannelItem.getChannelName().equals(lastChannelName)) {
                    lastChannelGroupIndex = liveChannelGroup.getGroupIndex();
                    lastLiveChannelIndex = liveChannelItem.getChannelIndex();
                    break;
                }
            }
            if (lastChannelGroupIndex != -1) break;
        }
        if (lastChannelGroupIndex == -1) {
            lastChannelGroupIndex = getFirstNoPasswordChannelGroup();
            if (lastChannelGroupIndex == -1) lastChannelGroupIndex = 0;
            lastLiveChannelIndex = 0;
        }

        livePlayerManager.init(mVideoView);
        showTime();
        showNetSpeed();
        tvLeftChannelListLayout.setVisibility(View.INVISIBLE);
        tvRightSettingLayout.setVisibility(View.INVISIBLE);

        liveChannelGroupAdapter.setNewData(liveChannelGroupList);
        selectChannelGroup(lastChannelGroupIndex, false, lastLiveChannelIndex);
    }

    private boolean isListOrSettingLayoutVisible() {
        return isSettingLayoutVisible() || isChanelListVisible();
    }

    private boolean isChanelListVisible() {
        return tvLeftChannelListLayout.getVisibility() == View.VISIBLE;
    }

    private boolean isSettingLayoutVisible() {
        return tvRightSettingLayout.getVisibility() == View.VISIBLE;
    }

    private void initLiveSettingGroupList() {
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList("线路选择", "画面比例", "播放解码", "超时换源", "偏好设置", "直播地址", "悬浮播放", "退出直播"));
        ArrayList<ArrayList<String>> itemsArrayList = new ArrayList<>();
        ArrayList<String> sourceItems = new ArrayList<>();
        ArrayList<String> scaleItems = new ArrayList<>(Arrays.asList("默认", "16:9", "4:3", "填充", "原始", "裁剪"));
        ArrayList<String> playerDecoderItems = new ArrayList<>(Arrays.asList("系统", "ijk硬解", "ijk软解", "exo", "阿里播放器"));
        ArrayList<String> timeoutItems = new ArrayList<>(Arrays.asList("5s", "10s", "15s", "20s", "25s", "30s"));
        ArrayList<String> personalSettingItems = new ArrayList<>(Arrays.asList("显示时间", "显示网速", "换台反转", "跨选分类"));
        itemsArrayList.add(sourceItems);
        itemsArrayList.add(scaleItems);
        itemsArrayList.add(playerDecoderItems);
        itemsArrayList.add(timeoutItems);
        itemsArrayList.add(personalSettingItems);
        itemsArrayList.add(new ArrayList<>());
        itemsArrayList.add(new ArrayList<>());
        itemsArrayList.add(new ArrayList<>());

        liveSettingGroupList.clear();
        for (int i = 0; i < groupNames.size(); i++) {
            LiveSettingGroup liveSettingGroup = new LiveSettingGroup();
            ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
            liveSettingGroup.setGroupIndex(i);
            liveSettingGroup.setGroupName(groupNames.get(i));
            for (int j = 0; j < itemsArrayList.get(i).size(); j++) {
                LiveSettingItem liveSettingItem = new LiveSettingItem();
                liveSettingItem.setItemIndex(j);
                liveSettingItem.setItemName(itemsArrayList.get(i).get(j));
                liveSettingItemList.add(liveSettingItem);
            }
            liveSettingGroup.setLiveSettingItems(liveSettingItemList);
            liveSettingGroupList.add(liveSettingGroup);
        }
        liveSettingGroupList.get(3).getLiveSettingItems().get(Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 5)).setItemSelected(true);
        liveSettingGroupList.get(4).getLiveSettingItems().get(0).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupList.get(4).getLiveSettingItems().get(1).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false));
        liveSettingGroupList.get(4).getLiveSettingItems().get(2).setItemSelected(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false));
        liveSettingGroupList.get(4).getLiveSettingItems().get(3).setItemSelected(Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false));
    }

    private void loadCurrentSourceList() {
        if (currentLiveChannelItem == null) {
            return;
        }
        ArrayList<String> currentSourceNames = currentLiveChannelItem.getChannelSourceNames();
        ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
        for (int j = 0; j < currentSourceNames.size(); j++) {
            LiveSettingItem liveSettingItem = new LiveSettingItem();
            liveSettingItem.setItemIndex(j);
            liveSettingItem.setItemName(currentSourceNames.get(j));
            liveSettingItemList.add(liveSettingItem);
        }
        liveSettingGroupList.get(0).setLiveSettingItems(liveSettingItemList);
    }

    void showTime() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_TIME, false)) {
            mHandler.post(mUpdateTimeRun);
            tvTime.setVisibility(View.VISIBLE);
        } else {
            mHandler.removeCallbacks(mUpdateTimeRun);
            tvTime.setVisibility(View.GONE);
        }
    }

    private Runnable mUpdateTimeRun = new Runnable() {
        @Override
        public void run() {
            Date day = new Date();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            tvTime.setText(df.format(day));
            mHandler.postDelayed(this, 1000);
        }
    };

    private void showNetSpeed() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false)) {
            mHandler.post(mUpdateNetSpeedRun);
            tvNetSpeed.setVisibility(View.VISIBLE);
        } else {
            mHandler.removeCallbacks(mUpdateNetSpeedRun);
            tvNetSpeed.setVisibility(View.GONE);
        }
    }

    private Runnable mUpdateNetSpeedRun = new Runnable() {
        @Override
        public void run() {
            if (mVideoView == null) return;
            tvNetSpeed.setText(String.format("%.2fMb/s", (float) mVideoView.getTcpSpeed() / 1024.0 / 1024.0));
            mHandler.postDelayed(this, 1000);
        }
    };

    private void showPasswordDialog(int groupIndex, int liveChannelIndex) {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE)
            mHandler.removeCallbacks(mHideChannelListRun);

        LivePasswordDialog dialog = new LivePasswordDialog(this);
        dialog.setOnListener(new LivePasswordDialog.OnListener() {
            @Override
            public void onChange(String password) {
                if (password.equals(liveChannelGroupList.get(groupIndex).getGroupPassword())) {
                    channelGroupPasswordConfirmed.add(groupIndex);
                    loadChannelGroupDataAndPlay(groupIndex, liveChannelIndex);
                } else {
                    Toast.makeText(App.getInstance(), "密码错误", Toast.LENGTH_SHORT).show();
                }

                if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE)
                    mHandler.postDelayed(mHideChannelListRun, 5000);
            }

            @Override
            public void onCancel() {
                if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
                    int groupIndex = liveChannelGroupAdapter.getSelectedGroupIndex();
                    liveChannelItemAdapter.setNewData(getLiveChannels(groupIndex));
                }
            }
        });
        dialog.show();
    }

    private void loadChannelGroupDataAndPlay(int groupIndex, int liveChannelIndex) {
        liveChannelItemAdapter.setNewData(getLiveChannels(groupIndex));
        if (groupIndex == currentChannelGroupIndex) {
            if (currentLiveChannelIndex > -1)
                mLiveChannelView.scrollToPosition(currentLiveChannelIndex);
            liveChannelItemAdapter.setSelectedChannelIndex(currentLiveChannelIndex);
        } else {
            mLiveChannelView.scrollToPosition(0);
            liveChannelItemAdapter.setSelectedChannelIndex(0);
        }

        if (liveChannelIndex > -1) {
            clickLiveChannel(liveChannelIndex);
            mChannelGroupView.scrollToPosition(groupIndex);
            mLiveChannelView.scrollToPosition(liveChannelIndex);
            playChannel(groupIndex, liveChannelIndex, false);
        }
    }

    private boolean isNeedInputPassword(int groupIndex) {
        if (liveChannelGroupList.isEmpty() || groupIndex == -1) {
            return false;
        }
        return !liveChannelGroupList.get(groupIndex).getGroupPassword().isEmpty() && !isPasswordConfirmed(groupIndex);
    }

    private boolean isPasswordConfirmed(int groupIndex) {
        for (Integer confirmedNum : channelGroupPasswordConfirmed) {
            if (confirmedNum == groupIndex) return true;
        }
        return false;
    }

    private ArrayList<LiveChannelItem> getLiveChannels(int groupIndex) {
        if (liveChannelGroupList.isEmpty()) {
            return new ArrayList<>();
        }
        if (!isNeedInputPassword(groupIndex)) {
            return liveChannelGroupList.get(groupIndex).getLiveChannels();
        } else {
            return new ArrayList<>();
        }
    }

    private Integer[] getNextChannel(int direction) {
        int channelGroupIndex = currentChannelGroupIndex;
        int liveChannelIndex = currentLiveChannelIndex;

        //跨选分组模式下跳过加密频道分组（遥控器上下键换台/超时换源）
        if (direction > 0) {
            liveChannelIndex++;
            if (liveChannelIndex >= getLiveChannels(channelGroupIndex).size()) {
                liveChannelIndex = 0;
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)) {
                    do {
                        channelGroupIndex++;
                        if (channelGroupIndex >= liveChannelGroupList.size()) channelGroupIndex = 0;
                    } while (!liveChannelGroupList.get(channelGroupIndex).getGroupPassword().isEmpty() || channelGroupIndex == currentChannelGroupIndex);
                }
            }
        } else {
            liveChannelIndex--;
            if (liveChannelIndex < 0) {
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)) {
                    do {
                        channelGroupIndex--;
                        if (channelGroupIndex < 0)
                            channelGroupIndex = liveChannelGroupList.size() - 1;
                    } while (!liveChannelGroupList.get(channelGroupIndex).getGroupPassword().isEmpty() || channelGroupIndex == currentChannelGroupIndex);
                }
                liveChannelIndex = getLiveChannels(channelGroupIndex).size() - 1;
            }
        }

        Integer[] groupChannelIndex = new Integer[2];
        groupChannelIndex[0] = channelGroupIndex;
        groupChannelIndex[1] = liveChannelIndex;

        return groupChannelIndex;
    }

    private int getFirstNoPasswordChannelGroup() {
        boolean empty = getLiveCollected().getLiveChannels().isEmpty();
        for (LiveChannelGroup liveChannelGroup : liveChannelGroupList) {
            if (liveChannelGroup.getGroupPassword().isEmpty()) {
                int groupIndex = liveChannelGroup.getGroupIndex();
                if (empty && groupIndex == 0) {
                    return 1;
                }
                return liveChannelGroup.getGroupIndex();
            }
        }
        return -1;
    }

    private boolean isCurrentLiveChannelValid() {
        if (currentLiveChannelItem == null && !isNeedInputPassword(currentLiveChannelIndex)) {
            Toast.makeText(App.getInstance(), "请先选择频道", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //计算两个时间相差的秒数
    public static long getTime(String startTime, String endTime) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long eTime = 0;
        try {
            eTime = df.parse(endTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long sTime = 0;
        try {
            sTime = df.parse(startTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = (eTime - sTime) / 1000;
        return diff;
    }

    private String durationToString(int duration) {
        String result = "";
        int dur = duration / 1000;
        int hour = dur / 3600;
        int min = (dur / 60) % 60;
        int sec = dur % 60;
        if (hour > 0) {
            if (min > 9) {
                if (sec > 9) {
                    result = hour + ":" + min + ":" + sec;
                } else {
                    result = hour + ":" + min + ":0" + sec;
                }
            } else {
                if (sec > 9) {
                    result = hour + ":" + "0" + min + ":" + sec;
                } else {
                    result = hour + ":" + "0" + min + ":0" + sec;
                }
            }
        } else {
            if (min > 9) {
                if (sec > 9) {
                    result = min + ":" + sec;
                } else {
                    result = min + ":0" + sec;
                }
            } else {
                if (sec > 9) {
                    result = "0" + min + ":" + sec;
                } else {
                    result = "0" + min + ":0" + sec;
                }
            }
        }
        return result;
    }

    public void showProgressBars(boolean show) {
        if (mVideoView.getDuration() <= 0L) {
            return;
        }
        sBar.requestFocus();
        if (show) {
            backcontroller.setVisibility(View.VISIBLE);
        } else {
            backcontroller.setVisibility(View.GONE);
        }
        if (show) {
            mHandler.postDelayed(liveProgressRunable, 1000);
            sBar.setProgress((int) mVideoView.getCurrentPosition());
            tv_currentpos.setText(durationToString((int) mVideoView.getCurrentPosition()));
            if (progressCountDownTimer != null) {
                progressCountDownTimer.cancel();
            }
            progressCountDownTimer = new CountDownTimer(5000, 1000) {//底部epg隐藏时间设定
                public void onTick(long j) {
                }

                public void onFinish() {
                    backcontroller.setVisibility(View.GONE);
                    mHandler.removeCallbacksAndMessages(liveProgressRunable);
                }
            };
            progressCountDownTimer.start();
        } else {
            mHandler.removeCallbacksAndMessages(liveProgressRunable);
        }
    }

    private final Runnable liveProgressRunable = new Runnable() {

        @Override
        public void run() {
            if (mVideoView != null) {
                sBar.setProgress((int) mVideoView.getCurrentPosition());
                tv_currentpos.setText(durationToString((int) mVideoView.getCurrentPosition()));
                mHandler.postDelayed(this, 1000);
            }
        }
    };


}