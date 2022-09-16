package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SpanUtils;
import com.github.tvbox.osc.R;

import org.jetbrains.annotations.NotNull;

public class AboutDialog extends BaseDialog {

    public AboutDialog(@NonNull @NotNull Context context) {
        super(context);
        setContentView(R.layout.dialog_about);
        TextView textView = findViewById(R.id.title1);

        SpanUtils.with(textView).append("新功能的开发占用了非常多的周末时间，一直用爱发电，不过也希望你们能赞赏我喝杯咖啡")
                        .append("\n").append("微信公众号搜索").append("安卓哥开发").setBold().setForegroundColor(Color.RED)
                        .append("可以联系我给软件提出自己的建议，我将在有限的时间里开发出新的功能").create();
    }
}