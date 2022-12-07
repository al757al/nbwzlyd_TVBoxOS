package com.gsoft.mitv;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.forcetech.android.ForceTV;


public class P2PService extends Service {

	private ForceTV forceTV;
	private IBinder binder;

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			binder = new LocalBinder();
		} catch (Throwable ignored) {
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		forceTV = new ForceTV();
		forceTV.start("p2p", 9906);
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		forceTV.stop();
		return super.onUnbind(intent);
	}

}
