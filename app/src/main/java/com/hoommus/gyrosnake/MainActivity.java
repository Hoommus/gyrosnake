package com.hoommus.gyrosnake;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.game_area);
        mControlsView = findViewById(R.id.fullscreen_content_controls);

        // Set up the user interaction to manually show or hide the system UI.


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPause() {
        surfaceView.pauseDrawing();
		surfaceView.pauseGame();
        super.onPause();
    }

//    @Override
//    protected void onStop() {
//    	surfaceView.pauseGame();
//        surfaceView.pauseDrawing();
//        super.onStop();
//    }

//    @Override
//    protected void onStart() {
//        surfaceView.resumeDrawing();
//        super.onStart();
//    }

    @Override
    protected void onResume() {

        surfaceView.resumeGame();
        surfaceView.resumeDrawing();
        super.onResume();
    }
}
