package com.hoommus.gyrosnake;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private View mControlsView;
    private SnakeView surfaceView;
    private static Handler handler;

	public static final int TOAST = 1;
	public static final int TEXT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case TOAST:
						Toast.makeText(findViewById(R.id.score).getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
						break;
					case TEXT:
						((TextView) findViewById(R.id.score)).setText("Score: " + msg.obj);
						break;
				}
			}
		};
        //handler.setScoreView(findViewById(R.id.score));
        surfaceView = findViewById(R.id.game_area);
        surfaceView.setMainUiHandler(handler);
        mControlsView = findViewById(R.id.fullscreen_content_controls);

        // Set up the user interaction to manually show or hide the system UI.


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }



    @Override
    protected void onPause() {
		super.onPause();
		surfaceView.pauseDrawing();
		surfaceView.pauseGame();
    }

    @Override
    protected void onStop() {
		super.onStop();
		surfaceView.pauseGame();
        surfaceView.pauseDrawing();
    }

    @Override
    protected void onStart() {
		super.onStart();
		surfaceView.resumeGame();
		surfaceView.resumeDrawing();

    }

    @Override
    protected void onResume() {
		super.onResume();
        surfaceView.resumeGame();
        surfaceView.resumeDrawing();

    }
}
