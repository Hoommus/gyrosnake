package com.hoommus.gyrosnake;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private View mControlsView;
    private SnakeView surfaceView;
    private UpdateUIHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new UpdateUIHandler(Looper.getMainLooper());

        surfaceView = findViewById(R.id.game_area);
        surfaceView.setHandler(handler);
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
