package com.hoommus.gyrosnake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Snake extends SurfaceView implements Runnable {
    private Thread thread = null;

    private Context context;

    // For tracking movement Heading
    // Start by heading to the right
    private Direction heading = Direction.RIGHT;

    // To hold the screen size in pixels
    private int screenX;
    private int screenY;

    // How long is the snake
    private int snakeLength;

    // The size in pixels of a segment
    private int blockSize;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int numBlocksHigh;

    private long nextFrameTime;
    // Update the game 10 times per second
    private final long FPS = 45;
// We will draw the frame much more often

    // How many points does the player have
    private int score;

    // The location in the grid of all the segments
    private int[] snakeXs;
    private int[] snakeYs;

    // Everything we need for drawing
    private volatile boolean isPlaying;

    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private Paint paint;

    @Override
    public void run() {

    }

    public enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

}
