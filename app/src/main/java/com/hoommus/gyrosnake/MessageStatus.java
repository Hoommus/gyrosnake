package com.hoommus.gyrosnake;


/**
 * Not enum, because of switch usage in MainActivity.onCreate()
 */
public class MessageStatus {
	public static final int TOAST = 1;
	public static final int SCORE = 2;
	public static final int GAME_OVER = 4;
	public static final int PAUSE = 8;
}
