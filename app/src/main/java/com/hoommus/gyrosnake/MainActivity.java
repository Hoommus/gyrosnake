package com.hoommus.gyrosnake;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
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

    @SuppressLint("HandlerLeak")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

						break;
					case MessageStatus.GAME_OVER:
						buildGameOverDialog(msg.arg1);
						break;
				}
			}
		};
        surfaceView = findViewById(R.id.game_area);
        surfaceView.setMainUiHandler(handler);
        Bundle args = new Bundle();
        args.putInt("mapwidth", 10);
        args.putInt("mapheight", 10);
        surfaceView.startNewGame(args);
        mControlsView = findViewById(R.id.fullscreen_content_controls);
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
