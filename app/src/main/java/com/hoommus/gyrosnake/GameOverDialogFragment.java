package com.hoommus.gyrosnake;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class GameOverDialogFragment extends DialogFragment {
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Game over. Your score: " + getArguments().getInt("score"))
				.setPositiveButton("Play again", (dialog, which) -> {
					if (getActivity() == null)
						return;
					SnakeView view = getActivity().findViewById(R.id.game_area);
					view.startNewGame(null);
				})
				.setNegativeButton("Do nothing", (dialog, which) -> {
					//dialog.cancel();
				});
		return builder.create();
	}
}
