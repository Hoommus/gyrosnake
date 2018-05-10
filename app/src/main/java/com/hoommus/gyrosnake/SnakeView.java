package com.hoommus.gyrosnake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.hoommus.gyrosnake.entities.MapEntity;
import com.hoommus.gyrosnake.entities.SnakeSegment;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SurfaceView to make drawing life easier
 */

public class SnakeView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private Context context;

    private Direction snakeDir = Direction.DOWN;
    // Controls snake movement speed int seconds.
    private int gamePace = 500;

    // Pixel measurements
    private int vPadding = 48;
    private int hPadding;
	private float controlPadding;

    // How long is the snake
    private int snakeLength;
    private int screenDensity;
    private int blockSize = 50;
    private MapEntity[][] matrix;
    private Rect map;

    // Update the game 45 times per second
    private final long FPS = 45;
    // We will draw the frame much more often

    // How many points does the player have
    private int score;

    // Thread management
    private ScheduledExecutorService executor;
	private ScheduledFuture drawingFuture;
	private ScheduledFuture snakeFuture;

	private LinkedList<SnakeSegment> snakeSegments;
    private volatile boolean isPlaying;

    private Paint fieldPaint;
    private Paint snakeBodyPaint;
    private Paint snakeStrokePaint;
    private Paint obstaclePaint;
    private Paint overlayPaint;
    private Paint overlayStrokePaint;

    private SurfaceHolder holder;

    public SnakeView(Context context) {
        super(context);
        this.context = context;
        initAuxilia();
    }

    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
		this.context = context;
        initAuxilia();
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
		this.context = context;
        initAuxilia();
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initAuxilia() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenDensity = metrics.densityDpi;
        blockSize = screenDensity / 6;
		hPadding = blockSize;
		vPadding = blockSize;
        controlPadding = screenDensity;
        fieldPaint         = new Paint(Paint.ANTI_ALIAS_FLAG);
        snakeBodyPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
        snakeStrokePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
        obstaclePaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
        overlayPaint       = new Paint(Paint.DITHER_FLAG);
        overlayStrokePaint = new Paint(Paint.DITHER_FLAG);

        overlayStrokePaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        overlayStrokePaint.setStyle(Paint.Style.STROKE);
        overlayStrokePaint.setAlpha(60);
        overlayStrokePaint.setStrokeCap(Paint.Cap.SQUARE);
        overlayPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
		overlayPaint.setAlpha(50);
        fieldPaint.setColor(Color.LTGRAY);
        snakeBodyPaint.setColor(getResources().getColor(R.color.colorSnake));
        snakeBodyPaint.setStyle(Paint.Style.FILL);
        snakeStrokePaint.setStyle(Paint.Style.STROKE);
        snakeStrokePaint.setColor(getResources().getColor(R.color.colorSnakeStroke));
        snakeStrokePaint.setStrokeWidth(4);
        snakeStrokePaint.setStrokeCap(Paint.Cap.BUTT);
        snakeStrokePaint.setStrokeJoin(Paint.Join.MITER);
        snakeStrokePaint.setStrokeMiter(4);
        obstaclePaint.setColor(Color.BLACK);

        this.setOnTouchListener((v, event) -> {
            final float x = event.getX();
            final float y = event.getY();

            final float width = this.getWidth();
            final float height = this.getHeight();

            if (x > width - controlPadding && y < height - controlPadding && y > controlPadding)
            	changeDirection(Direction.RIGHT);
            else if (x < controlPadding && y < height - controlPadding && y > controlPadding)
            	changeDirection(Direction.LEFT);
            else if (y < controlPadding)
            	changeDirection(Direction.UP);
            else if (y > height - controlPadding)
            	changeDirection(Direction.DOWN);

            return v.performClick();
        });


        snakeDir = Direction.DOWN;

        snakeSegments = new LinkedList<>();
        snakeSegments.add(new SnakeSegment(5, 5));
        snakeSegments.add(new SnakeSegment(6, 5));
        snakeSegments.add(new SnakeSegment(6, 6));
        snakeSegments.add(new SnakeSegment(6, 7));
        snakeSegments.add(new SnakeSegment(6, 8));

		executor = new ScheduledThreadPoolExecutor(10);
	}

    private void createMap(int width, int height) {
    	if (width < 5)
    		width = 5;
    	if (height < 5)
    		height = 5;
        matrix = new MapEntity[height][width];
        for (MapEntity[] line : matrix)
        	Arrays.fill(line, MapEntity.EMPTY);
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
        spawnFood();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        createMap(10, 20);
        this.getHolder().addCallback(this);
    }

    /**
     * Game itself
     */
    @Override
    public void run() {
        moveSnake();
    }

	/**
	 * Gets next head coordinates depending on direction and checks block there.
	 * Function exists only to incorporate that if-statement for map bounds
	 */
    private MapEntity whatsAhead(int nextX, int nextY) {
		if (nextY < 0 || nextY >= matrix.length || nextX < 0 || nextX >= matrix[0].length
				|| matrix[nextY][nextX] == MapEntity.SNAKE)
			return MapEntity.OBSTACLE;
		return matrix[nextY][nextX];
	}

	/**
	 * Because of linked list usage, last element is actually snake's head. And first - it's tail
	 */
	private void moveSnake() {
		try {
			final SnakeSegment tail = snakeSegments.getFirst();
			final SnakeSegment head = snakeSegments.getLast();

			int nextX = head.getX() + (snakeDir == Direction.LEFT ? -1 : 0) + (snakeDir == Direction.RIGHT ? 1 : 0);
			int nextY = head.getY() + (snakeDir == Direction.UP ? -1 : 0) + (snakeDir == Direction.DOWN ? 1 : 0);

			switch (whatsAhead(nextX, nextY)) {
				case FOOD:
					snakeSegments.add(new SnakeSegment(nextX, nextY));
					spawnFood();
					break;
				case EMPTY:
					tail.setX(nextX);
					tail.setY(nextY);
					snakeSegments.removeFirst();
					snakeSegments.addLast(tail);
					break;
				case OBSTACLE:
				case SNAKE:
				default:
					//Toast.makeText(this.getContext(), "Game Over: " + nextY + ", " + nextX, Toast.LENGTH_LONG).show();
					break;
            }
		} catch (Throwable t) {
			t.printStackTrace();
		}
    }

    private void spawnFood() {
		Random random = new Random();
		int x = random.nextInt(matrix[0].length);
		int y = random.nextInt(matrix.length);

		matrix[y][x] = MapEntity.FOOD;
	}

    private void endGame() {

	}

    private void updateView(SurfaceHolder holder)
    {
		Canvas canvas = holder.lockCanvas();
    	try {
			drawMap(canvas);
			drawSnake(canvas);
			drawOverlay(canvas);

		} catch (Throwable t) {
    		t.printStackTrace();
		}
		finally {
    		if (canvas != null)
				holder.unlockCanvasAndPost(canvas);
		}
    }

    private void drawMap(Canvas canvas) {
		if (map == null)
			map = new Rect(hPadding, vPadding, this.getWidth() - hPadding, this.getHeight() - vPadding);
		canvas.drawColor(Color.DKGRAY);
        canvas.drawRect(map, fieldPaint);
    }

    private void drawSnake(Canvas canvas) {
        int xTop;
        int yTop;
        int xBot;
        int yBot;

        for (SnakeSegment segment : snakeSegments) {
            xTop = hPadding + segment.getX() * blockSize;
            yTop = vPadding + segment.getY() * blockSize;
            xBot = xTop + blockSize;
            yBot = yTop + blockSize;
            canvas.drawRect(xTop, yTop, xBot, yBot, snakeBodyPaint);
            canvas.drawRect(xTop, yTop, xBot, yBot, snakeStrokePaint);
        }
    }

    private void drawOverlay(Canvas canvas) {
		int width = this.getWidth();
		int height = this.getHeight();

		// top
    	canvas.drawRect(0, 0, width, controlPadding, overlayPaint);
    	canvas.drawRect(0, 0, width, controlPadding, overlayStrokePaint);
    	// left
    	canvas.drawRect(0, controlPadding, controlPadding, height - controlPadding, overlayPaint);
    	canvas.drawRect(0, controlPadding, controlPadding, height - controlPadding, overlayStrokePaint);
    	// right
    	canvas.drawRect(width - controlPadding, controlPadding, width, height - controlPadding, overlayPaint);
    	canvas.drawRect(width - controlPadding, controlPadding, width, height - controlPadding, overlayStrokePaint);
    	// bottom
    	canvas.drawRect(0, height - controlPadding, width, height, overlayPaint);
    	canvas.drawRect(0, height - controlPadding, width, height, overlayStrokePaint);
	}

    private void changeDirection(Direction d) {
        if (this.snakeDir != d && d != snakeDir.getOpposite())
            this.snakeDir = d;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        updateView(holder);
        this.holder = holder;
        resumeDrawing();
        resumeGame();
	}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void pauseDrawing() {
		if (drawingFuture != null)
			drawingFuture.cancel(false);

	}

	public void resumeDrawing() {
		drawingFuture = executor.scheduleAtFixedRate(() -> updateView(holder), 1, 1000 / FPS, TimeUnit.MILLISECONDS);
	}

	public void pauseGame() {
		if (snakeFuture != null)
			snakeFuture.cancel(true);
	}

	public void resumeGame() {
		snakeFuture = executor.scheduleAtFixedRate(this::moveSnake, 2000, gamePace, TimeUnit.MILLISECONDS);
	}

    @Override
    public boolean performClick() {
        return super.performClick();
    }


}
