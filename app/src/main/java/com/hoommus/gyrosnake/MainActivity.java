package com.hoommus.gyrosnake;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private ImageButton controlButton;
	private SensorManager sensorManager;
    private SnakeView surfaceView;
    private static Handler handler;

    @SuppressLint("HandlerLeak")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		surfaceView = findViewById(R.id.game_area);
        handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MessageStatus.TOAST:
						Toast.makeText(findViewById(R.id.score).getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
						break;
					case MessageStatus.SCORE:
						((TextView) findViewById(R.id.score)).setText("Score: " + msg.obj);
						break;
					case MessageStatus.PAUSE:
						surfaceView.pauseGame();
						break;
					case MessageStatus.GAME_OVER:
						vibrator.vibrate(50);
						buildGameOverDialog(msg.arg1);
						break;
				}
			}
		};
		surfaceView.setMainUiHandler(handler);
		sensorManager.registerListener(surfaceView, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(surfaceView, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
		surfaceView.pauseGame();
		new GameInstructionsDialogFragment().show(getSupportFragmentManager(), "tutorial");
        controlButton = findViewById(R.id.control_button);
		controlButton.setOnClickListener(v -> {
			if (surfaceView.isPlaying()) {
				Toast.makeText(getApplicationContext(), "Game paused.", Toast.LENGTH_LONG).show();
				controlButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
				surfaceView.pauseGame();
			} else {
				controlButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
				surfaceView.resumeGame();
			}
		});
    }

	private void buildGameOverDialog(int score) {
		GameOverDialogFragment dialog = new GameOverDialogFragment();
		surfaceView.pauseGame();
		Bundle bundle = new Bundle();
		bundle.putInt("score", score);
		dialog.setArguments(bundle);
		dialog.show(getSupportFragmentManager(), "game over");
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
