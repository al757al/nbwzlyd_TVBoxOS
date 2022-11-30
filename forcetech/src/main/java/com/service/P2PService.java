package com.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.anymediacloud.iptv.standard.ForceTV;

public class P2PService extends Service {

	private ForceTV forceTV;

	@Override
	public IBinder onBind(Intent arg0) {
		forceTV = new ForceTV();
		forceTV.start("p2p", 9906);
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		forceTV.stop();
		return super.onUnbind(intent);
	}

}
