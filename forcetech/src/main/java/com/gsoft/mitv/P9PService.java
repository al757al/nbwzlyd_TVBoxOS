//package com.gsoft.mitv;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.IBinder;
//
//import com.forcetech.android.ForceTV;
//
//
//public class P9PService extends Service {
//
//	private ForceTV forceTV;
//	private IBinder binder;
//
//	@Override
//	public void onCreate() {
//		super.onCreate();
//		try {
//			binder = new LocalBinder();
//		} catch (Throwable ignored) {
//		}
//	}
//	@Override
//	public IBinder onBind(Intent arg0) {
//		forceTV=new ForceTV();
//		forceTV.start("p9p", 9913);
//		return binder;
//	}
//	@Override
//	public boolean onUnbind(Intent intent) {
//		forceTV.stop();
//		return super.onUnbind(intent);
//	}
//
//}
