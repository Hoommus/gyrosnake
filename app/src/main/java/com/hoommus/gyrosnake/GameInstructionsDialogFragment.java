package com.hoommus.gyrosnake;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class GameInstructionsDialogFragment extends DialogFragment {
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Hold phone parallel to the ground and use it's gyroscope to control the snake.\n\n" +
				"Rotate along longer side of the phone to move the snake left or right\n" +
				"Rotate across it to move the snake upwards or downwards")
				.setPositiveButton("Got it", (dialog, which) -> {
					if (getActivity() == null)
						return;
					SnakeView view = getActivity().findViewById(R.id.game_area);
					view.invalidate();
					view.resumeGame();
					//view.startNewGame(null);
				});
		return builder.create();
	}
}
