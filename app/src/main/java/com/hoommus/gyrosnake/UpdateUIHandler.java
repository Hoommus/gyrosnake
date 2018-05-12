package com.hoommus.gyrosnake;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;
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

	public void setScoreView(TextView scoreView) {
		//this.scoreView = scoreView;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
			case TOAST:
				//Toast.makeText(scoreView.getContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
				break;
			case TEXT:
				//scoreView.setText((String) msg.obj);
				break;
		}
	}
}
