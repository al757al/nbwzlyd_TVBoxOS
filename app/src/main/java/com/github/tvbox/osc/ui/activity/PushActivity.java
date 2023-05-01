package com.github.tvbox.osc.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.tv.QRCodeGen;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import me.jessyan.autosize.utils.AutoSizeUtils;

public class PushActivity extends BaseActivity {
    private ImageView ivQRCode;
    private TextView tvAddress;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_push;
    }

    @Override
    protected void init() {
        initView();
        initData();
    }

    private void initView() {
        ivQRCode = findViewById(R.id.ivQRCode);
        tvAddress = findViewById(R.id.tvAddress);
        refreshQRCode();
        findViewById(R.id.pushLocal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ClipboardManager manager = (ClipboardManager) PushActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (manager != null) {
                        if (manager.hasPrimaryClip() && manager.getPrimaryClip() != null && manager.getPrimaryClip().getItemCount() > 0) {
                            ClipData.Item addedText = manager.getPrimaryClip().getItemAt(0);
                            String clipText = addedText.getText().toString().trim();
                            Intent newIntent = new Intent(mContext, DetailActivity.class);
                            newIntent.putExtra("id", clipText);
                            newIntent.putExtra("sourceKey", "push_agent");
                            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PushActivity.this.startActivity(newIntent);
                        }
                    }
                } catch (Throwable th) {

                }
            }
        });
        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.et_push_url);
                Editable text = editText.getText();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(mContext, "请输入推送链接", Toast.LENGTH_SHORT).show();
                    return;
                }

                String textStr = text.toString().trim();
                if (textStr.startsWith("token@")) {
                    EventBus.getDefault().post(new RefreshEvent(RefreshEvent.ALI_TOKEN, textStr.split("@")[1]));
                    finish();
                    return;
                }

                String pushString = text.toString().trim();
                LinkedHashMap<String, String> resultHashMap = readLine(pushString);
                if (!resultHashMap.isEmpty()) {
                    VodInfo vodInfo = new VodInfo();
                    ArrayList<VodInfo.VodSeries> data = new ArrayList<>();
                    vodInfo.sourceKey = "push_clip_board";
                    vodInfo.seriesMap = new LinkedHashMap<>();
                    for (Map.Entry<String, String> entry : resultHashMap.entrySet()) {
                        String name = entry.getKey();
                        String url = entry.getValue();
                        VodInfo.VodSeries vodSeries = new VodInfo.VodSeries();
                        vodSeries.name = name;
                        vodSeries.url = url;
                        data.add(vodSeries);
                    }
                    vodInfo.playFlag = vodInfo.sourceKey;
                    vodInfo.seriesMap.put(vodInfo.sourceKey, data);
                    Intent newIntent = new Intent(mContext, PlayActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("VodInfo", vodInfo);
                    newIntent.putExtras(bundle);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PushActivity.this.startActivity(newIntent);
                } else {
                    Intent newIntent = new Intent(mContext, DetailActivity.class);
                    newIntent.putExtra("id", text.toString().trim());
                    newIntent.putExtra("sourceKey", "push_agent");
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PushActivity.this.startActivity(newIntent);
                }
            }
        });
    }

    private void refreshQRCode() {
        String address = ControlManager.get().getAddress(false);
        tvAddress.setText(String.format("扫描上方二维码访问地址\n%s", address));
        ivQRCode.setImageBitmap(QRCodeGen.generateBitmap(address, AutoSizeUtils.mm2px(this, 300), AutoSizeUtils.mm2px(this, 300), 4));
    }

    private Serializable buildVodInfo(VodInfo vodInfo) {
        vodInfo.seriesFlags = new ArrayList<>();
        vodInfo.seriesFlags.add(new VodInfo.VodSeriesFlag(vodInfo.sourceKey));
        vodInfo.seriesMap = new LinkedHashMap<>();
        ArrayList<VodInfo.VodSeries> data = new ArrayList<>();
        VodInfo.VodSeries vodSeries = new VodInfo.VodSeries();
        vodSeries.name = vodInfo.name;
        vodSeries.url = vodInfo.id;
        data.add(vodSeries);
        vodInfo.seriesMap.put(vodInfo.sourceKey, data);


        Bundle bundle = new Bundle();
        bundle.putString("id", vodInfo.id);
        bundle.putString("sourceKey", vodInfo.sourceKey);
        jumpActivity(DetailActivity.class, bundle);


        return vodInfo;
    }

    private LinkedHashMap<String, String> readLine(String content) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes(UTF-8)), UTF-8));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().contains("http")) {
                    String[] split = line.split(",");
                    linkedHashMap.put(split[0], split[1].trim());
                }
            }
        } catch (Exception e) {
        }
        return linkedHashMap;
    }


    private void initData() {

    }
}