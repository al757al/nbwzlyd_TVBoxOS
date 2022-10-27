//package com.github.tvbox.osc.ui.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.annotation.Nullable;
//import androidx.fragment.app.FragmentActivity;
//
//import com.github.tvbox.osc.R;
//import com.github.tvbox.osc.base.BaseActivity;
//
///**
// * <pre>
// *     author : derek
// *     time   : 2022/10/26
// *     desc   :
// *     version:
// * </pre>
// */
//public class SplashActivity extends FragmentActivity {
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        //关键代码
////        if (!isTaskRoot()) {
////            Intent intent = getIntent();
////            if (intent != null) {
////                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
////                    finish();
////                    return;
////                }
////            }
////        }
//        finish();
//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
//        finish();
//    }
//
//}
