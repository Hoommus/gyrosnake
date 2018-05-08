package com.hoommus.gyrosnake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hoommus.gyrosnake.entities.SnakeSegment;

import java.util.LinkedList;
import java.util.Random;

/**
 * SurfaceView to make drawing life easier
 */

public class SnakeView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private Thread thread = null;

    private Context context;

    // For tracking movement Heading
    // Start by heading to the right
    private Direction snakeDirection = Direction.RIGHT;

    // Pixel measurements
    private int width;
    private int height;
    private int verticalPadding = 48;
    private int horizontalPadding;

    // How long is the snake
    private int snakeLength;

    private int gridSize;
    private int blockSize;
    private int[][] matrix;
    // Update the game 45 times per second
    private final long FPS = 45;
    // We will draw the frame much more often

    // How many points does the player have
    private int score;

    private LinkedList<SnakeSegment> snakeSegments;
    // Everything we need for drawing
    private volatile boolean isPlaying;

    // TODO: Remove this one
    private Random random = new Random();

    private Paint paint;

    public SnakeView(Context context) {
        super(context);
        initAuxilia(20, 30);
    }

    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAuxilia(20, 30);
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAuxilia(20, 30);
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initAuxilia(int width, int height) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.DKGRAY);
        this.getHolder().addCallback(this);

        thread = new Thread(() -> {
            Canvas canvas = this.getHolder().lockCanvas();
            canvas.drawRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            getHolder().unlockCanvasAndPost(canvas);
            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        this.setOnTouchListener((v, event) -> {
            random = new Random();
            return v.performClick();
        });

        snakeSegments = new LinkedList<>();
        snakeSegments.add(new SnakeSegment(0, 0));
        snakeSegments.add(new SnakeSegment(0, 1));
        snakeSegments.add(new SnakeSegment(0, 2));
    }

    private void createMap(int width, int height) {
        matrix = new int[height][width];

        int actualMapHeight = (matrix.length    + 2) * blockSize;
        int actualMapWidth  = (matrix[0].length + 2) * blockSize;

        horizontalPadding = (this.getWidth() - actualMapWidth) / 2;
        verticalPadding = (this.getHeight() - actualMapHeight) / 2;
    }

    @Override
    public void run() {

    }

    private void updateGame(SurfaceHolder holder)
    {

    }

    private void drawMap(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();

        holder.unlockCanvasAndPost(canvas);
    }

    private void drawSnake(SurfaceHolder holder) {

    }

    public void changeDirection(Direction d) {
        if (this.snakeDirection != d)
            this.snakeDirection = d;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawRect(50, 50, 250, 250, paint);
        holder.unlockCanvasAndPost(canvas);
        drawMap(holder);
        drawSnake(holder);
        thread.run();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

}
