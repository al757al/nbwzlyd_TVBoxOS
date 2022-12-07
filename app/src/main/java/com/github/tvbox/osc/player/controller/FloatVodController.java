package com.github.tvbox.osc.player.controller;

/**
 * <pre>
 *     author : derek
 *     time   : 2022/12/03
 *     desc   :
 *     version:
 * </pre>
 */

import static xyz.doikki.videoplayer.util.PlayerUtils.stringForTime;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DiffUtil;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.subtitle.widget.SimpleSubtitleView;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.ui.view.ChoosePlayPopUp;
import com.github.tvbox.osc.ui.view.PlayerMoreFucPop;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.PlayerHelper;
import com.github.tvbox.osc.util.ScreenUtils;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.util.PlayerUtils;

public class FloatVodController extends BaseController {

    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");


    SeekBar mSeekBar;
    TextView mCurrentTime;
    TextView mTotalTime;
    boolean mIsDragging;
    LinearLayout mProgressRoot;
    TextView mProgressText;
    ImageView mProgressIcon;
    LinearLayout mBottomRoot;
    LinearLayout mTopRoot1;
    //    LinearLayout mParseRoot;
//    TvRecyclerView mGridView;
    //    TextView mPlayTitle;
    TextView mPlayTitle1;
    TextView mTvSpeedPlay;
    ImageView mLockView;
    TextView mNextBtn;
    TextView mPreBtn;
    LockRunnable lockRunnable = new LockRunnable();
    //    TextView mPlayerScaleBtn;
    public TextView mPlayerSpeedBtn;
    TextView mPlayerBtn;
    TextView mPlayerIJKBtn;
    TextView mPlayerRetry;
    TextView mPlayrefresh;
    private PlayerMoreFucPop mPlayerMoreFuc;

    //    public TextView mPlayerTimeStartBtn;
//    public TextView mPlayerTimeSkipBtn;
    //    public TextView mPlayerTimeStepBtn;
//    public TextView mPlayerTimeResetBtn;
    TextView mPlayPauseTime;
    //    TextView mPlayLoadNetSpeed;
//    TextView mVideoSize;
//    private View backBtn;//返回键
//    private boolean isClickBackBtn;
    private HorizontalScrollView mHorizontalScrollView;

    private boolean mIsFullScreen = false;
    private boolean isLock = false;
    public SimpleSubtitleView mSubtitleView;
    //    TextView mZimuBtn;
    private TextView mMiniProgressTextView;
    private boolean mShowMiniProgress;

    Handler myHandle;
    Runnable myRunnable;
    int myHandleSeconds = 6000;//闲置多少毫秒秒关闭底栏  默认6秒
    private TextView mNetSpeed;
    private View line;

    int videoPlayState = 0;

    private Runnable myRunnable2 = new Runnable() {
        @Override
        public void run() {
            handleLastedTime();
            String speed = PlayerHelper.getDisplaySpeed(mControlWrapper.getTcpSpeed());
//            mPlayLoadNetSpeed.setText(speed);
            mNetSpeed.setText(speed);
//            String width = Integer.toString(mControlWrapper.getVideoSize()[0]);
//            String height = Integer.toString(mControlWrapper.getVideoSize()[1]);
//            mVideoSize.setText("[ " + width + " X " + height + " ]");
            mHandler.postDelayed(this, 1000);
        }
    };

    private void showLockView() {
        mLockView.setVisibility(ScreenUtils.isTv(getContext()) ? INVISIBLE : VISIBLE);
        mHandler.removeCallbacks(lockRunnable);
        mHandler.postDelayed(lockRunnable, 3000);
    }

    public FloatVodController(@NonNull @NotNull Context context) {
        super(context);
        setCanChangePosition(false);
        setEnableInNormal(false);
        setGestureEnabled(false);
        mHandlerCallback = new HandlerCallback() {
            @Override
            public void callback(Message msg) {
                switch (msg.what) {
                    case 1000: { // seek 刷新
                        mProgressRoot.setVisibility(VISIBLE);
                        break;
                    }
                    case 1001: { // seek 关闭
                        mProgressRoot.setVisibility(GONE);
                        break;
                    }
                    case 1002: { // 显示底部菜单
                        mBottomRoot.setVisibility(VISIBLE);
                        mHorizontalScrollView.scrollTo(0, 0);
                        mTopRoot1.setVisibility(VISIBLE);
                        showNowTime(true);
                        mPlayPauseTime.setVisibility(VISIBLE);
                        mPlayTitle1.setVisibility(View.VISIBLE);
                        mNetSpeed.setVisibility(VISIBLE);
                        new Handler().postDelayed(() -> {
                            mBottomRoot.requestFocus();
                        }, 20);
//                        backBtn.setVisibility(ScreenUtils.isTv(context) ? INVISIBLE : VISIBLE);
                        showLockView();
                        break;
                    }
                    case 1003: { // 隐藏底部菜单
                        mBottomRoot.setVisibility(GONE);
                        mTopRoot1.setVisibility(GONE);
//                        backBtn.setVisibility(INVISIBLE);
                        showNowTime(false);
//                        mPlayPauseTime.setVisibility(GONE);
                        mNetSpeed.setVisibility(INVISIBLE);
                        break;
                    }
                    case 1004: { // 设置速度
                        if (isInPlaybackState()) {
                            try {
                                float speed = (float) mPlayerConfig.getDouble("sp");
                                mControlWrapper.setSpeed(speed);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else
                            mHandler.sendEmptyMessageDelayed(1004, 100);
                        break;
                    }
                }
            }
        };
    }

    private void showNowTime(boolean forceShow) {
        handleLastedTime();
        if (forceShow) {
            mPlayPauseTime.setVisibility(View.VISIBLE);
            return;
        }
        boolean showTime = Hawk.get(HawkConfig.VIDEO_SHOW_TIME, false);
        if (showTime) {
            mPlayPauseTime.setVisibility(View.VISIBLE);
        } else {
            mPlayPauseTime.setVisibility(View.GONE);
        }
    }

    //显示最新时间
    private void handleLastedTime() {
        Date date = new Date();
        mPlayPauseTime.setText(timeFormat.format(date));
        if (mTopRoot1.getVisibility() == VISIBLE) {
            mPlayPauseTime.setAlpha(1);
        } else {
            mPlayPauseTime.setAlpha(0.6f);
        }
    }

    private boolean isFastSpeed;//是否在倍速播放

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        //半屏宽度
//        int halfScreen = PlayerUtils.getScreenWidth(getContext(), true) / 2;
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            fastSpeedPlay();
        }
    }

    private void fastSpeedPlay() {
        if (!mIsStartProgress) {
            return;
        }
        isFastSpeed = true;
        mControlWrapper.setSpeed(3.0f);
        mTvSpeedPlay.setText("当前3倍速播放中 " + mCurrentTime.getText() + "/" + mTotalTime.getText());
        if (mTvSpeedPlay.getVisibility() != View.VISIBLE) {
            mTvSpeedPlay.setVisibility(VISIBLE);
        }
    }

    private void stopFastSpeedPlay() {
        isFastSpeed = false;
        try {
            float speed = (float) mPlayerConfig.getDouble("sp");
            mControlWrapper.setSpeed(speed);
            if (mTvSpeedPlay.getVisibility() == View.VISIBLE) {
                mTvSpeedPlay.setVisibility(INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mGestureDetector.onTouchEvent(event)) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopFastSpeedPlay();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    boolean needShowLine() {
        return mPlayPauseTime.getVisibility() == VISIBLE && mMiniProgressTextView.getVisibility() == VISIBLE;
    }

    public void setIsFullScreen(boolean isFullScreen) {
        mIsFullScreen = isFullScreen;
        if (mPlayPauseTime == null) {
            return;
        }
        if (!isFullScreen) {
            mPlayPauseTime.setVisibility(GONE);
            mMiniProgressTextView.setVisibility(GONE);
        } else {
            boolean showTime = Hawk.get(HawkConfig.VIDEO_SHOW_TIME, false);
            if (showTime) {
                mPlayPauseTime.setVisibility(VISIBLE);
            }
            if (mShowMiniProgress) {
                mMiniProgressTextView.setVisibility(VISIBLE);
            }

        }
    }

    @Override
    protected void initView() {
        super.initView();
        setForceImmersive(Hawk.get(HawkConfig.IMMERSIVE_SWITCH, false));
        mCurrentTime = findViewById(R.id.curr_time);
        mTotalTime = findViewById(R.id.total_time);
//        mPlayTitle = findViewById(R.id.tv_info_name);
        mHorizontalScrollView = findViewById(R.id.horizontalScrollView);
        mLockView = findViewById(R.id.tv_lock);
        mLockView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isLock = !isLock;
                mControlWrapper.setLocked(isLock);
                mLockView.setImageResource(isLock ? R.drawable.icon_lock : R.drawable.icon_unlock);
                if (isLock) {
                    Message obtain = Message.obtain();
                    obtain.what = 1003;//隐藏底部菜单
                    mHandler.sendMessage(obtain);
                }
                showLockView();
            }
        });
        View rootView = findViewById(R.id.rootView);

//        findViewById(R.id.float_view).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                floatListener.onFloatClick(true);
//            }
//        });
        mPlayTitle1 = findViewById(R.id.tv_info_name1);
        mTvSpeedPlay = findViewById(R.id.tv_speed_play);
        mSeekBar = findViewById(R.id.seekBar);
        mProgressRoot = findViewById(R.id.tv_progress_container);
        mProgressIcon = findViewById(R.id.tv_progress_icon);
        mProgressText = findViewById(R.id.tv_progress_text);
        mBottomRoot = findViewById(R.id.bottom_container);
        mTopRoot1 = findViewById(R.id.tv_top_l_container);
//        backBtn = findViewById(R.id.tv_back);
        line = findViewById(R.id.line);
        mMiniProgressTextView = findViewById(R.id.tiny_progress);
        mShowMiniProgress = Hawk.get(HawkConfig.MINI_PROGRESS, false);
//        backBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getContext() instanceof Activity) {
//                    isClickBackBtn = true;
//                    ((Activity) getContext()).onBackPressed();
//                }
//            }
//        });
//        mParseRoot = findViewById(R.id.parse_root);
//        mGridView = findViewById(R.id.mGridView);
        mPlayerRetry = findViewById(R.id.play_retry);
        mPlayrefresh = findViewById(R.id.play_refresh);
        mNextBtn = findViewById(R.id.play_next);
        mPreBtn = findViewById(R.id.play_pre);
//        TextView choosePlay = findViewById(R.id.choose_play);
//        choosePlay.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                choosePlay(v);
//            }
//        });
//        mPlayerScaleBtn = findViewById(R.id.play_scale);
        mPlayerSpeedBtn = findViewById(R.id.play_speed);
        mPlayerBtn = findViewById(R.id.play_player);
        mPlayerIJKBtn = findViewById(R.id.play_ijk);
//        mPlayerTimeStartBtn = findViewById(R.id.play_time_start);
//        mPlayerTimeSkipBtn = findViewById(R.id.play_time_end);
//        mPlayerTimeStepBtn = findViewById(R.id.play_time_step);
//        mPlayerTimeResetBtn = findViewById(R.id.play_time_reset);
        mPlayPauseTime = findViewById(R.id.tv_sys_time);
//        mPlayLoadNetSpeed = findViewById(R.id.tv_play_load_net_speed);
        mNetSpeed = findViewById(R.id.tv_net_speed);
//        mVideoSize = findViewById(R.id.tv_videosize);
        mSubtitleView = findViewById(R.id.subtitle_view);
//        mZimuBtn = findViewById(R.id.zimu_select);
//        findViewById(R.id.more_fuc).setOnClickListener(v ->
//                mPlayerMoreFuc
//                        .setOnItemClickListener(textView -> {
//                            if (textView.getId() == R.id.play_scale) {
//                                scaleVideoView(textView);
//                            }
//                            if (textView.getId() == R.id.play_speed) {
//                                setSpeedVideoView(textView);
//                            }
//                            if (textView.getId() == R.id.audio_track_select) {
//                                setAudioTrack();
//                            }
//                            if (textView.getId() == R.id.tiny_progress) {
//                                mShowMiniProgress = !mShowMiniProgress;
//                                textView.setText(mShowMiniProgress ? "迷你进度开" : "迷你进度关");
//                                Hawk.put(HawkConfig.MINI_PROGRESS, mShowMiniProgress);
//                            }
//                            return null;
//                        })
//                        .setPopupGravity(Gravity.END)
//                        .showPopupWindow());

        updateSubInfoTextSize(8);

        myHandle = new Handler();
        myRunnable = new Runnable() {
            @Override
            public void run() {
                hideBottom();
            }
        };

        mPlayPauseTime.post(new Runnable() {
            @Override
            public void run() {
                mHandler.post(myRunnable2);
            }
        });

//        mGridView.setLayoutManager(new V7LinearLayoutManager(getContext(), 0, false));
//        ParseAdapter parseAdapter = new ParseAdapter();
//        parseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
//                ParseBean parseBean = parseAdapter.getItem(position);
//                // 当前默认解析需要刷新
//                int currentDefault = parseAdapter.getData().indexOf(ApiConfig.get().getDefaultParse());
//                parseAdapter.notifyItemChanged(currentDefault);
//                ApiConfig.get().setDefaultParse(parseBean);
//                parseAdapter.notifyItemChanged(position);
//                listener.changeParse(parseBean);
//                hideBottom();
//            }
//        });
//        mGridView.setAdapter(parseAdapter);
//        parseAdapter.setNewData(ApiConfig.get().getParseBeanList());
//
//        mParseRoot.setVisibility(VISIBLE);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                myHandle.removeCallbacks(myRunnable);
                myHandle.postDelayed(myRunnable, myHandleSeconds);
                long duration = mControlWrapper.getDuration();
                long newPosition = (duration * progress) / seekBar.getMax();
                String currentTime = stringForTime((int) newPosition);
                if (mCurrentTime != null)
                    mCurrentTime.setText(currentTime);
                mMiniProgressTextView.setText(currentTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsDragging = true;
                mControlWrapper.stopProgress();
                mControlWrapper.stopFadeOut();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myHandle.removeCallbacks(myRunnable);
                myHandle.postDelayed(myRunnable, myHandleSeconds);
                long duration = mControlWrapper.getDuration();
                long newPosition = (duration * seekBar.getProgress()) / seekBar.getMax();
                mControlWrapper.seekTo((int) newPosition);
                mIsDragging = false;
                mControlWrapper.startProgress();
                mControlWrapper.startFadeOut();
            }
        });
//        mPlayerRetry.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.replay(true);
//                hideBottom();
//            }
//        });
        mPlayrefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.replay(false);
                hideBottom();
            }
        });
        mNextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.playNext(false);
                hideBottom();
            }
        });
        mPreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.playPre();
                hideBottom();
            }
        });
//        mPlayerScaleBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                scaleVideoView();
//            }
//        });

        // takagen99: Add long press to reset speed
        mPlayerSpeedBtn.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    mPlayerConfig.put("sp", 1.0f);
                    updatePlayerCfgView();
                    listener.updatePlayerCfg();
                    mControlWrapper.setSpeed(1.0f);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        mPlayerSpeedBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSpeedVideoView((TextView) v);
            }
        });
        mPlayerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                myHandle.removeCallbacks(myRunnable);
                myHandle.postDelayed(myRunnable, myHandleSeconds);
                try {
                    int playerType = mPlayerConfig.getInt("pl");
                    ArrayList<Integer> exsitPlayerTypes = PlayerHelper.getExistPlayerTypes();
                    int playerTypeIdx = 0;
                    int playerTypeSize = exsitPlayerTypes.size();
                    for (int i = 0; i < playerTypeSize; i++) {
                        if (playerType == exsitPlayerTypes.get(i)) {
                            if (i == playerTypeSize - 1) {
                                playerTypeIdx = 0;
                            } else {
                                playerTypeIdx = i + 1;
                            }
                        }
                    }
                    playerType = exsitPlayerTypes.get(playerTypeIdx);
                    mPlayerConfig.put("pl", playerType);
                    updatePlayerCfgView();
                    listener.updatePlayerCfg();
                    listener.replay(false);
//                    hideBottom();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mPlayerBtn.requestFocus();
                mPlayerBtn.requestFocusFromTouch();
            }
        });

        mPlayerBtn.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                myHandle.removeCallbacks(myRunnable);
                myHandle.postDelayed(myRunnable, myHandleSeconds);
                FastClickCheckUtil.check(view);
                try {
                    int playerType = mPlayerConfig.getInt("pl");
                    int defaultPos = 0;
                    ArrayList<Integer> players = PlayerHelper.getExistPlayerTypes();
                    ArrayList<Integer> renders = new ArrayList<>();
                    for (int p = 0; p < players.size(); p++) {
                        renders.add(p);
                        if (players.get(p) == playerType) {
                            defaultPos = p;
                        }
                    }
                    SelectDialog<Integer> dialog = new SelectDialog<>(mActivity);
                    dialog.setTip("请选择播放器");
                    dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                        @Override
                        public void click(Integer value, int pos) {
                            try {
                                dialog.cancel();
                                int thisPlayType = players.get(pos);
                                if (thisPlayType != playerType) {
                                    mPlayerConfig.put("pl", thisPlayType);
                                    updatePlayerCfgView();
                                    listener.updatePlayerCfg();
                                    listener.replay(false);
//                                    hideBottom();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mPlayerBtn.requestFocus();
                            mPlayerBtn.requestFocusFromTouch();
                        }

                        @Override
                        public String getDisplay(Integer val) {
                            Integer playerType = players.get(val);
                            return PlayerHelper.getPlayerName(playerType);
                        }
                    }, new DiffUtil.ItemCallback<Integer>() {
                        @Override
                        public boolean areItemsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                            return oldItem.intValue() == newItem.intValue();
                        }

                        @Override
                        public boolean areContentsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                            return oldItem.intValue() == newItem.intValue();
                        }
                    }, renders, defaultPos);
                    dialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        mPlayerIJKBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                myHandle.removeCallbacks(myRunnable);
                myHandle.postDelayed(myRunnable, myHandleSeconds);
                try {
                    String ijk = mPlayerConfig.getString("ijk");
                    List<IJKCode> codecs = ApiConfig.get().getIjkCodes();
                    for (int i = 0; i < codecs.size(); i++) {
                        if (ijk.equals(codecs.get(i).getName())) {
                            if (i >= codecs.size() - 1)
                                ijk = codecs.get(0).getName();
                            else {
                                ijk = codecs.get(i + 1).getName();
                            }
                            break;
                        }
                    }
                    mPlayerConfig.put("ijk", ijk);
                    updatePlayerCfgView();
                    listener.updatePlayerCfg();
                    listener.replay(false);
//                    hideBottom();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mPlayerIJKBtn.requestFocus();
                mPlayerIJKBtn.requestFocusFromTouch();
            }
        });
//        增加播放页面片头片尾时间重置
//        mPlayerTimeResetBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myHandle.removeCallbacks(myRunnable);
//                myHandle.postDelayed(myRunnable, myHandleSeconds);
//                try {
//                    mPlayerConfig.put("et", 0);
//                    mPlayerConfig.put("st", 0);
//                    updatePlayerCfgView();
//                    listener.updatePlayerCfg();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        mPlayerTimeStartBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                myHandle.removeCallbacks(myRunnable);
//                myHandle.postDelayed(myRunnable, myHandleSeconds);
//                try {
//                    mPlayerConfig.put("st", PlayerUtils.getCurrentPlayPositionTime((int) mControlWrapper.getCurrentPosition()));
//                    updatePlayerCfgView();
//                    listener.updatePlayerCfg();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        mPlayerTimeStartBtn.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                try {
//                    ToastUtils.showShort("已重置开始时间");
//                    mPlayerConfig.put("st", 0);
//                    updatePlayerCfgView();
//                    listener.updatePlayerCfg();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                return true;
//            }
//        });
//        mPlayerTimeSkipBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                myHandle.removeCallbacks(myRunnable);
//                myHandle.postDelayed(myRunnable, myHandleSeconds);
//                try {
//                    if (mControlWrapper.getDuration() <= 0) {
//                        return;
//                    }
//                    int endTime = (int) (mControlWrapper.getDuration() - mControlWrapper.getCurrentPosition());
//                    mPlayerConfig.put("et", PlayerUtils.getCurrentPlayPositionTime(endTime));
//                    updatePlayerCfgView();
//                    listener.updatePlayerCfg();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        mPlayerTimeSkipBtn.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                try {
//                    ToastUtils.showShort("已重置结束时间");
//                    mPlayerConfig.put("et", 0);
//                    updatePlayerCfgView();
//                    listener.updatePlayerCfg();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                return true;
//            }
//        });
//        mZimuBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FastClickCheckUtil.check(view);
//                listener.selectSubtitle();
//                hideBottom();
//            }
//        });
//        mZimuBtn.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                mSubtitleView.setVisibility(View.GONE);
//                mSubtitleView.destroy();
//                mSubtitleView.clearSubtitleCache();
//                mSubtitleView.isInternal = false;
//                hideBottom();
//                Toast.makeText(getContext(), "字幕已关闭", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });
    }

    private void setAudioTrack() {
        listener.selectAudioTrack();
        hideBottom();
    }

    private void setSpeedVideoView(TextView textView) {
        myHandle.removeCallbacks(myRunnable);
        myHandle.postDelayed(myRunnable, myHandleSeconds);
        try {
            float speed = (float) mPlayerConfig.getDouble("sp");
            speed += 0.25f;
            if (speed > 3)
                speed = 0.5f;
            mPlayerConfig.put("sp", speed);
            updatePlayerCfgView();
            textView.setText("x" + mPlayerConfig.getDouble("sp"));
            listener.updatePlayerCfg();
            mControlWrapper.setSpeed(speed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void scaleVideoView(TextView textView) {
        myHandle.removeCallbacks(myRunnable);
        myHandle.postDelayed(myRunnable, myHandleSeconds);
        try {
            int scaleType = mPlayerConfig.getInt("sc");
            scaleType++;
            if (scaleType > 5)
                scaleType = 0;
            mPlayerConfig.put("sc", scaleType);
            updatePlayerCfgView();
            textView.setText(PlayerHelper.getScaleName(mPlayerConfig.getInt("sc")));
            listener.updatePlayerCfg();
            mControlWrapper.setScreenScaleType(scaleType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void choosePlay(View view) {
        ChoosePlayPopUp playPopUp = new ChoosePlayPopUp(mActivity);
        playPopUp.setPopupGravity(Gravity.TOP).showPopupWindow(view);

    }

    public void updateSubInfoTextSize(int size) {
//        int subtitleTextSize = SubtitleHelper.getTextSize(getRootView());
        int textSize = Math.min(Math.max(size, 3), 14);
        mSubtitleView.setTextSize(textSize);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.float_player_vod_control_view;
    }

    public void showParse(boolean userJxList) {
//        mParseRoot.setVisibility(userJxList ? VISIBLE : GONE);
    }

    private JSONObject mPlayerConfig = null;

    public void setPlayerConfig(JSONObject playerCfg) {
        this.mPlayerConfig = playerCfg;
        updatePlayerCfgView();
        mPlayerMoreFuc = new PlayerMoreFucPop(getContext(), mPlayerConfig);
    }

    void updatePlayerCfgView() {
        try {
            int playerType = mPlayerConfig.getInt("pl");
            mPlayerBtn.setText(PlayerHelper.getPlayerName(playerType));
//            mPlayerScaleBtn.setText(PlayerHelper.getScaleName(mPlayerConfig.getInt("sc")));
            mPlayerIJKBtn.setText(mPlayerConfig.getString("ijk"));
            mPlayerIJKBtn.setVisibility(playerType == 1 ? VISIBLE : GONE);
//            mPlayerScaleBtn.setText(PlayerHelper.getScaleName(mPlayerConfig.getInt("sc")));
            mPlayerSpeedBtn.setText("x" + mPlayerConfig.getDouble("sp"));
//            updateStartAndEndTime();
//            mPlayerTimeStepBtn.setText(Hawk.get(HawkConfig.PLAY_TIME_STEP, 5) + "s");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void updateStartAndEndTime() {
//        try {
//            mPlayerTimeStartBtn.setText("片头" + PlayerUtils.stringForTime(mPlayerConfig.getInt("st") * 1000));
//            int endTime = mPlayerConfig.getInt("et");
//            if (endTime > 0 && mControlWrapper.getDuration() > 0) {
//                float showEndTime = (mControlWrapper.getDuration() - endTime * 1000L);
//                mPlayerTimeSkipBtn.setText("片尾" + PlayerUtils.stringForTime((int) showEndTime));
//            } else {
//                mPlayerTimeSkipBtn.setText("片尾" + PlayerUtils.stringForTime(endTime * 1000));
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }

    public void setTitle(String playTitleInfo) {
//        mPlayTitle.setText(playTitleInfo);
        mPlayTitle1.setText(playTitleInfo);
    }

    public void setUrlTitle(String playTitleInfo) {
        mPlayTitle1.setText(playTitleInfo);
    }

    public void resetSpeed() {
        skipEnd = true;
        mHandler.removeMessages(1004);
        mHandler.sendEmptyMessageDelayed(1004, 100);
    }

    public interface VodControlListener {
        void playNext(boolean rmProgress);

        void playPre();

        void prepared();

        void changeParse(ParseBean pb);

        void updatePlayerCfg();

        void replay(boolean replay);

        void errReplay();

        void selectSubtitle();

        void selectAudioTrack();
    }

    public void setListener(VodControlListener listener) {
        this.listener = listener;
    }

    private VodControlListener listener;

    private boolean skipEnd = true;

    @Override
    protected void setProgress(int duration, int position) {

        if (mIsDragging) {
            return;
        }
        super.setProgress(duration, position);
        if (skipEnd && position != 0 && duration != 0) {
            int et = 0;
            try {
                et = mPlayerConfig.getInt("et");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (et > 0 && position + (et * 1000) >= duration) {
                skipEnd = false;
                listener.playNext(true);
            }
        }
        String curTime = PlayerUtils.stringForTime(position);
        String totalTime = PlayerUtils.stringForTime(duration);
        mCurrentTime.setText(curTime);
        mTotalTime.setText(totalTime);
        mMiniProgressTextView.setText(curTime + "/" + totalTime);
        if (mShowMiniProgress) {
            mMiniProgressTextView.setVisibility(VISIBLE);
        } else {
            mMiniProgressTextView.setVisibility(GONE);
        }
        if (needShowLine()) {
            if (line.getVisibility() != View.VISIBLE) {
                line.setVisibility(VISIBLE);
            }
        } else {
            if (line.getVisibility() == View.VISIBLE) {
                line.setVisibility(GONE);
            }
        }
        if (isFastSpeed) {
            mTvSpeedPlay.setText("当前3倍速播放中 " + mCurrentTime.getText() + "/" + mTotalTime.getText());
        }
        if (duration > 0) {
            mSeekBar.setEnabled(true);
            int pos = (int) (position * 1.0 / duration * mSeekBar.getMax());
            mSeekBar.setProgress(pos);
        } else {
            mSeekBar.setEnabled(false);
        }
        int percent = mControlWrapper.getBufferedPercentage();
        if (percent >= 95) {
            mSeekBar.setSecondaryProgress(mSeekBar.getMax());
        } else {
            mSeekBar.setSecondaryProgress(percent * 10);
        }
    }

    private boolean simSlideStart = false;
    private int simSeekPosition = 0;
    private long simSlideOffset = 0;

    public void tvSlideStop() {
        if (!simSlideStart)
            return;
        mControlWrapper.seekTo(simSeekPosition);
        if (!mControlWrapper.isPlaying())
            mControlWrapper.start();
        simSlideStart = false;
        simSeekPosition = 0;
        simSlideOffset = 0;
    }

    public void tvSlideStart(int dir) {
        int duration = (int) mControlWrapper.getDuration();
        if (duration <= 0)
            return;
        if (!simSlideStart) {
            simSlideStart = true;
        }
        // 每次10秒
        simSlideOffset += (10000.0f * dir);
        int currentPosition = (int) mControlWrapper.getCurrentPosition();
        int position = (int) (simSlideOffset + currentPosition);
        if (position > duration) position = duration;
        if (position < 0) position = 0;
        updateSeekUI(currentPosition, position, duration);
        simSeekPosition = position;
    }

    @Override
    public void updateSeekUI(int curr, int seekTo, int duration) {
        super.updateSeekUI(curr, seekTo, duration);
        if (seekTo > curr) {
            mProgressIcon.setImageResource(R.drawable.icon_pre);
        } else {
            mProgressIcon.setImageResource(R.drawable.icon_back);
        }
        mProgressText.setText(PlayerUtils.stringForTime(seekTo) + " / " + PlayerUtils.stringForTime(duration));
        mHandler.sendEmptyMessage(1000);
        mHandler.removeMessages(1001);
        mHandler.sendEmptyMessageDelayed(1001, 1000);
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        videoPlayState = playState;
        switch (playState) {
            case VideoView.STATE_IDLE:
                break;
            case VideoView.STATE_PLAYING:
                startProgress();
                mNetSpeed.setVisibility(GONE);
                break;
            case VideoView.STATE_PAUSED:
                mTopRoot1.setVisibility(GONE);
//                mPlayPauseTime.setVisibility(GONE);
                mPlayTitle1.setVisibility(VISIBLE);
                mNetSpeed.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_ERROR:
                listener.errReplay();
                break;
            case VideoView.STATE_PREPARED:
//                mPlayLoadNetSpeed.setVisibility(GONE);
                listener.prepared();
                break;
            case VideoView.STATE_BUFFERED:
//                mPlayLoadNetSpeed.setVisibility(GONE);
                break;
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_BUFFERING:
//                updateStartAndEndTime();
//                mPlayLoadNetSpeed.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                listener.playNext(true);
                break;
        }
    }

    boolean isBottomVisible() {
        return mBottomRoot.getVisibility() == VISIBLE;
    }

    void showBottom() {
        mHandler.removeMessages(1003);
        mHandler.sendEmptyMessage(1002);
    }

    void hideBottom() {
        mHandler.removeMessages(1002);
        mHandler.sendEmptyMessage(1003);
    }

    private boolean shortPress = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                event.startTracking();
                if (event.getRepeatCount() == 0) {
                    shortPress = true;
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            shortPress = false;
            fastSpeedPlay();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (shortPress) {
                if (!isBottomVisible()) {
                    showBottom();
                    myHandle.postDelayed(myRunnable, myHandleSeconds);
                    return true;
                }
            } else {
                if (isFastSpeed) {
                    stopFastSpeedPlay();
                }
            }
            shortPress = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        myHandle.removeCallbacks(myRunnable);
        if (super.onKeyEvent(event)) {
            return true;
        }
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (isBottomVisible()) {
            myHandle.postDelayed(myRunnable, myHandleSeconds);
            return super.dispatchKeyEvent(event);
        }
        boolean isInPlayback = isInPlaybackState();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (isInPlayback) {
                    tvSlideStart(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1);
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (isInPlayback) {
                    togglePlay();
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_MENU) {
                if (!isBottomVisible()) {
                    showBottom();
                    myHandle.postDelayed(myRunnable, myHandleSeconds);
                    return true;
                }
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (isInPlayback) {
                    tvSlideStop();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        myHandle.removeCallbacks(myRunnable);
        if (!isBottomVisible()) {
            showBottom();
            // 闲置计时关闭
            myHandle.postDelayed(myRunnable, myHandleSeconds);
        } else {
            hideBottom();
        }
        return true;
    }

    private class LockRunnable implements Runnable {

        @Override
        public void run() {
            mLockView.setVisibility(INVISIBLE);

        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void destroy() {
        super.destroy();
        mHandlerCallback = null;
        myHandle.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onBackPressed() {
//        if (isClickBackBtn) {
//            isClickBackBtn = false;
//            if (isBottomVisible()) {
//                hideBottom();
//            }
//            return false;
//        }
//        if (super.onBackPressed()) {
//            return true;
//        }
//        if (isBottomVisible()) {
//            hideBottom();
//            return true;
//        }
        return false;
    }


}

