package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SpanUtils;
import com.github.tvbox.osc.BuildConfig;
import com.github.tvbox.osc.R;

import org.jetbrains.annotations.NotNull;

public class AboutDialog extends BaseDialog {

    public AboutDialog(@NonNull @NotNull Context context) {
        super(context);
        setContentView(R.layout.dialog_about);
        TextView textView = findViewById(R.id.title1);

        SpanUtils.with(textView).append("版本号 V"+ BuildConfig.VERSION_NAME+"更新日志:")
                .append("\n").append("1.设置页UI优化，感谢双喜大兄弟\n2.增加清除缓存功能，让你的app重获新生\n3.优化遥控器焦点，点开就能看到我上次选的哪个条目" +
                        "\n4.进度条有颜色了，视频进度一看便知\n5.修复内存泄漏，占用内存少了\n5.优化了线路刷新逻辑，不会闪动了" +
                        "\n7.支持远程推送多仓库链接了~ \n8.手机支持拖拽对仓库进行排序了(电视不支持)~").create();
    }
}