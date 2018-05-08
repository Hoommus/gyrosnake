package com.hoommus.gyrosnake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hoommus.gyrosnake.entities.MapEntity;
import com.hoommus.gyrosnake.entities.SnakeSegment;

import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SurfaceView to make drawing life easier
 */

public class SnakeView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private Thread thread = null;
    private Thread graphicsThread;

    private Context context;

    // For tracking movement Heading
    // Start by heading to the right
    private Direction snakeDirection = Direction.RIGHT;

    // Pixel measurements
    private int width;
    private int height;
    private int vPadding = 48;
    private int hPadding;

    // How long is the snake
    private int snakeLength;
    private int screenDensity;
    private int blockSize = 50;
    private MapEntity[][] matrix;
    // Update the game 45 times per second
    private final long FPS = 45;
    // We will draw the frame much more often

    // How many points does the player have
    private int score;

    private ScheduledThreadPoolExecutor graphicsUpdater;

    private LinkedList<SnakeSegment> snakeSegments;
    // Everything we need for drawing
    private volatile boolean isPlaying;

    // TODO: Remove this one
    private Random random = new Random();

    private Paint fieldPaint;
    private Paint snakePaint;
    private Paint obstaclePaint;
    private SurfaceHolder holder;

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
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenDensity = metrics.densityDpi;
        blockSize = screenDensity / 6;
        fieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        snakePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        obstaclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        fieldPaint.setColor(Color.LTGRAY);
        snakePaint.setColor(Color.parseColor("#478A00"));
        obstaclePaint.setColor(Color.BLACK);

        graphicsUpdater = new ScheduledThreadPoolExecutor(4);
        graphicsUpdater.setKeepAliveTime(4000, TimeUnit.MILLISECONDS);
        graphicsUpdater.scheduleAtFixedRate(() -> updateView(holder), 1, 1000 / FPS, TimeUnit.MILLISECONDS);


        this.setOnTouchListener((v, event) -> {
            this.updateView(holder);
            this.moveSnake();
            return v.performClick();
        });

        snakeDirection = Direction.DOWN;

        snakeSegments = new LinkedList<>();
        snakeSegments.add(new SnakeSegment(0, 0));
        snakeSegments.add(new SnakeSegment(0, 1));
        snakeSegments.add(new SnakeSegment(0, 2));
    }

    private void createMap(int width, int height) {
        matrix = new MapEntity[height][width];

        int actualMapHeight = (matrix.length    + 2) * blockSize;
        int actualMapWidth  = (matrix[0].length + 2) * blockSize;

        while (actualMapHeight > this.getHeight() - screenDensity / 12 || actualMapWidth > this.getWidth() - screenDensity / 12)
        {
            blockSize--;
            actualMapHeight = (matrix.length    + 2) * blockSize;
            actualMapWidth  = (matrix[0].length + 2) * blockSize;
        }

        hPadding = (this.getWidth() - actualMapWidth) / 2;
        vPadding = (this.getHeight() - actualMapHeight) / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        createMap(15, 20);
        this.getHolder().addCallback(this);
    }

    /**
     * Game thread itself
     */
    @Override
    public void run() {
        moveSnake();

    }

    private void moveSnake() {
        SnakeSegment first = snakeSegments.getFirst();
        SnakeSegment last = snakeSegments.getLast();

        switch (snakeDirection) {
            case UP:
                first.setY(last.getY() - 1);
                break;
            case DOWN:
                first.setY(last.getY() + 1);
                break;
            case LEFT:
                first.setX(last.getX() - 1);
                break;
            case RIGHT:
                first.setX(last.getX() - 1);
                break;
        }
        snakeSegments.removeFirst();
        snakeSegments.addLast(first);
    }

    private void updateView(SurfaceHolder holder)
    {
        drawMap(holder);
        drawSnake(holder);
    }

    private void drawMap(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();

        canvas.drawRect(0, 0, this.getHeight(), this.getWidth(), obstaclePaint);
        canvas.drawRect(hPadding, vPadding, this.getWidth() - hPadding, this.getHeight() - vPadding, fieldPaint);

        holder.unlockCanvasAndPost(canvas);
    }

    private void drawSnake(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();

        int xTop;
        int yTop;
        int xBot;
        int yBot;

        for (SnakeSegment segment : snakeSegments) {
            xTop = hPadding + segment.getX() * blockSize - blockSize / 2;
            yTop = vPadding + segment.getY() * blockSize - blockSize / 2;
            xBot = xTop + blockSize;
            yBot = yTop + blockSize;
            canvas.drawRect(xTop, yTop, xBot, yBot, snakePaint);
        }

        holder.unlockCanvasAndPost(canvas);
    }

    public void changeDirection(Direction d) {
        if (this.snakeDirection != d)
            this.snakeDirection = d;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawMap(holder);
        drawSnake(holder);
        this.holder = holder;
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
