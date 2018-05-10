package com.hoommus.gyrosnake;

public enum Direction {
	UP,
	RIGHT,
	DOWN,
	LEFT;

	public Direction getOpposite() {
		switch (this) {
			case RIGHT:
				return LEFT;
			case LEFT:
				return RIGHT;
			case DOWN:
				return UP;
			default:
				return DOWN;
		}
	}
}