package com.hoommus.gyrosnake;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;


public class UpdateUIHandler extends Handler {
	public static final int TOAST = 1;
	public static final int TEXT = 2;

	public UpdateUIHandler() {
		super();
	}

	public UpdateUIHandler(Callback callback) {
		super(callback);
	}

	public UpdateUIHandler(Looper looper) {
		super(looper);
	}

	public UpdateUIHandler(Looper looper, Callback callback) {
		super(looper, callback);
	}

	@Override
	public void handleMessage(Message msg) {
		Looper.prepare();
		switch (msg.what) {
			case TOAST:
				this.post(() -> ((Toast) msg.obj).show());
				break;
			case TEXT:

				break;
		}
		Looper.loop();
	}
}
