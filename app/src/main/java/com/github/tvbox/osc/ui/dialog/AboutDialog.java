package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SpanUtils;
import com.github.tvbox.osc.BuildConfig;
import com.github.tvbox.osc.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class AboutDialog extends BaseDialog {

    public AboutDialog(@NonNull @NotNull Context context) {
        super(context);
        setContentView(R.layout.dialog_about);
        TextView textView = findViewById(R.id.title1);

        SpanUtils.with(textView).append("版本号 V"+ BuildConfig.VERSION_NAME+"更新日志:")
                .append("\n").append("1.调整首页历史等按钮的大小，适配TV更大气\n2.仓库推送顺序改为正向排序\n3.首页长按遥控器菜单键跳转设置页，短按切换首页源" +
                        "\n4.修复覆盖安装点击设置页会崩溃的问题\n5.删除内置赞赏码和公众号，起了一个非常不好的头，抱歉\n5.支持反编译内置仓库，beta版本，自行研究不一定生效" +
                        "\n7.支持首页换线路了，不用再跳转设置页了，撒花~").create();
    }
}