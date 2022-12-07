package com.forcetech.android;

public class ForceTV {
	public native int start(int port, int memory);

	public native int stop();

	public void start(String lib, int port) {
		System.loadLibrary(lib);
		try {
			start(port, 20971520);
		} catch (Exception e) {
		}
	}
}
