package com.hoommus.gyrosnake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hoommus.gyrosnake.entities.MapEntity;
import com.hoommus.gyrosnake.entities.SnakeSegment;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * SurfaceView to make drawing life easier 🌚
 */

public class SnakeView extends SurfaceView implements Runnable, SurfaceHolder.Callback, SensorEventListener {
	private Handler mainUiHandler;
	private Thread gameThread;
	private Thread graphicsThread;

    private Direction snakeDir = Direction.DOWN;
    // Controls snake movement speed int seconds.
    // Yeah, I know it's final at the moment
    private final int gamePace = 400;

    private int vPadding = 48;
    private int hPadding;
	private float controlPadding;

    private int screenDensity;
    private int blockSize = 50;
    private MapEntity[][] matrix;
    private Rect map;

    // Update the game 45 times per second
    private final long FPS = 45;
    // We will draw the frame much more often

    // How many points does the player have
    private Integer score;

	private final LinkedList<SnakeSegment> snakeSegments = new LinkedList<>();
    private volatile boolean isPlaying = false;
    private volatile boolean isDrawing = true;

    private float xyzPivot[] = new float[3];

    private Paint fieldPaint;
    private Paint snakeBodyPaint;
    private Paint snakeStrokePaint;
    private Paint obstaclePaint;
    private Paint overlayPaint;
    private Paint overlayStrokePaint;
    private Paint foodPaint;

    private Bundle args = new Bundle();

    private SurfaceHolder holder;

    public SnakeView(Context context) {
        super(context);
        initAuxilia();
    }

    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAuxilia();
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAuxilia();
    }

    public SnakeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initAuxilia() {
        initPaint();

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
	}

	public void setMainUiHandler(Handler mainUiHandler) {
    	this.mainUiHandler = mainUiHandler;
	}

	private void initPaint() {
		fieldPaint         = new Paint(Paint.ANTI_ALIAS_FLAG);
		snakeBodyPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
		snakeStrokePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
		obstaclePaint      = new Paint(Paint.ANTI_ALIAS_FLAG);
		overlayPaint       = new Paint(Paint.DITHER_FLAG);
		overlayStrokePaint = new Paint(Paint.DITHER_FLAG);
		foodPaint          = new Paint(Paint.ANTI_ALIAS_FLAG);

		overlayStrokePaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
		overlayStrokePaint.setStyle(Paint.Style.STROKE);
		overlayStrokePaint.setAlpha(60);
		overlayStrokePaint.setStrokeCap(Paint.Cap.SQUARE);
		overlayPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
		overlayPaint.setAlpha(50);
		fieldPaint.setColor(Color.LTGRAY);
		foodPaint.setColor(Color.YELLOW);
		snakeBodyPaint.setColor(getResources().getColor(R.color.colorSnake));
		snakeBodyPaint.setStyle(Paint.Style.FILL);
		snakeStrokePaint.setStyle(Paint.Style.STROKE);
		snakeStrokePaint.setColor(getResources().getColor(R.color.colorSnakeStroke));
		snakeStrokePaint.setStrokeWidth(4);
		snakeStrokePaint.setStrokeCap(Paint.Cap.BUTT);
		snakeStrokePaint.setStrokeJoin(Paint.Join.MITER);
		snakeStrokePaint.setStrokeMiter(4);
		obstaclePaint.setColor(Color.BLACK);
	}

	private void createSnake() {
    	int mapCenterX = matrix[0].length / 2;
    	int mapCenterY = matrix.length / 2;

		snakeDir = Direction.DOWN;

		synchronized (snakeSegments) {
            snakeSegments.clear();
            snakeSegments.add(new SnakeSegment(mapCenterX, mapCenterY - 1));
            snakeSegments.add(new SnakeSegment(mapCenterX, mapCenterY));
            snakeSegments.add(new SnakeSegment(mapCenterX, mapCenterY + 1));
        }
		Message msg = mainUiHandler.obtainMessage(MessageStatus.TOAST);
		msg.obj = "Snake head at (" + snakeSegments.getLast().getX() + ", " + snakeSegments.getLast().getY() + ")";
		mainUiHandler.sendMessage(msg);
	}

	private void createMap() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		screenDensity = metrics.densityDpi;
		blockSize = screenDensity / 6;
		hPadding = blockSize;
		vPadding = blockSize;
		controlPadding = screenDensity;

        for (MapEntity[] line : matrix)
        	Arrays.fill(line, MapEntity.EMPTY);
        int actualMapHeight = (matrix.length    ) * blockSize;
        int actualMapWidth  = (matrix[0].length ) * blockSize;

        while (actualMapHeight > this.getHeight() - screenDensity / 12 || actualMapWidth > this.getWidth() - screenDensity / 12)
        {
            blockSize--;
            actualMapHeight = (matrix.length    ) * blockSize;
            actualMapWidth  = (matrix[0].length ) * blockSize;
        }

        hPadding = (this.getWidth() - actualMapWidth) / 2;
        vPadding = (this.getHeight() - actualMapHeight) / 2;
		spawnFood();
    }

    public void startNewGame(Bundle args) {
    	int width = 5;
    	int height = 5;
    	if (args != null) {
			height = args.getInt("mapheight");
			width = args.getInt("mapwidth");
		} else if (matrix != null) {
    		height = matrix.length;
    		width = matrix[0].length;
		}
		score = 0;
    	matrix = new MapEntity[height][width];
    	createMap();
		createSnake();

		isDrawing = true;
		isPlaying = true;
		if (graphicsThread == null) {
			graphicsThread = new Thread(this::drawScene);
			graphicsThread.setDaemon(true);
			graphicsThread.start();
		}
		if (gameThread == null) {
			gameThread = new Thread(this::run);
			gameThread.start();
		} else
		    resumeGame();
	}



    /**
     * Game itself
     */
    @Override
    public void run() {
		while (true) {
			try {
				if (isPlaying) {
					moveSnake();
					Thread.sleep(gamePace);
				} else {
					synchronized (this) {
						wait();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
    }

    public void drawScene() {
		while (true) {
			try {
				if (isDrawing) {
					updateView(holder);
					try {
						Thread.sleep(1000 / FPS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					synchronized (this) {
						wait();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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

			Message msg;

			switch (whatsAhead(nextX, nextY)) {
				case FOOD:
					snakeSegments.add(new SnakeSegment(nextX, nextY));
					matrix[nextY][nextX] = MapEntity.EMPTY;
					spawnFood();
					score++;
					msg = mainUiHandler.obtainMessage(MessageStatus.TOAST);
					msg.obj = "Food eaten.";
					mainUiHandler.sendMessage(msg);
					msg = mainUiHandler.obtainMessage(MessageStatus.SCORE);
					msg.obj = score;
					mainUiHandler.sendMessage(msg);
					break;
				case EMPTY:
					tail.setX(nextX);
					tail.setY(nextY);
					snakeSegments.removeFirst();
					snakeSegments.addLast(tail);
					break;
				case OBSTACLE:
				case SNAKE:
					msg = mainUiHandler.obtainMessage(MessageStatus.GAME_OVER);
					msg.obj = score;
					msg.arg1 = score;
					mainUiHandler.sendMessage(msg);
					break;
            }
		} catch (Throwable t) {
			t.printStackTrace();
		}
    }

    private void spawnFood() {
		Random random = new Random();
		matrix[random.nextInt(matrix.length)][random.nextInt(matrix[0].length)] = MapEntity.FOOD;
	}

    private void updateView(SurfaceHolder holder)
    {
    	if (holder == null)
    		return;
		Canvas canvas = holder.lockCanvas();
    	try {
			drawMap(canvas);
			drawSnake(canvas);
			drawOverlay(canvas);
		} catch (Throwable t) {
    		t.printStackTrace();
		} finally {
    		if (canvas != null )
				holder.unlockCanvasAndPost(canvas);
		}
	}

    private void drawMap(Canvas canvas) {
		if (map == null)
			map = new Rect(hPadding, vPadding, this.getWidth() - hPadding, this.getHeight() - vPadding);
		canvas.drawColor(Color.DKGRAY);
        canvas.drawRect(map, fieldPaint);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] == MapEntity.FOOD) {
					int xTop = hPadding + j * blockSize;
					int yTop = vPadding + i * blockSize;
					int xBot = xTop + blockSize;
					int yBot = yTop + blockSize;
					canvas.drawRect(xTop, yTop, xBot, yBot, foodPaint);
					canvas.drawRect(xTop, yTop, xBot, yBot, snakeStrokePaint);
				}
			}
		}
    }

    private void drawSnake(Canvas canvas) {
        int xTop;
        int yTop;
        int xBot;
        int yBot;
		synchronized (snakeSegments) {
       		List<SnakeSegment> copy = Collections.unmodifiableList(snakeSegments);

            for (SnakeSegment segment : copy) {
                xTop = hPadding + segment.getX() * blockSize;
                yTop = vPadding + segment.getY() * blockSize;
                xBot = xTop + blockSize;
                yBot = yTop + blockSize;
                canvas.drawRect(xTop, yTop, xBot, yBot, snakeBodyPaint);
                canvas.drawRect(xTop, yTop, xBot, yBot, snakeStrokePaint);
            }
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
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
        args.putInt("mapwidth", 20);
        args.putInt("mapheight", 20);
		startNewGame(args);
		this.getHolder().addCallback(this);
	}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
	}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		//this.holder = holder;
		updateView(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { }

    public void pauseDrawing() {
		isDrawing = false;
	}

	public synchronized void resumeDrawing() {
		if (graphicsThread != null && graphicsThread.getState() == Thread.State.WAITING)
			this.notify();
		isDrawing = true;
	}

	public void pauseGame() {
		isPlaying = false;
	}

	public synchronized void resumeGame() {
		if (gameThread != null && gameThread.getState() == Thread.State.WAITING)
			this.notify();
		else if (gameThread != null && gameThread.getState() == Thread.State.NEW)
			gameThread.start();
		isPlaying = true;
	}

    @Override
    public boolean performClick() {
        return super.performClick();
    }

	private float gravity[];
	private float magnetic[];
	private float mR[] = new float[9];
	private float mI[] = new float[9];
	private float orientation[] = new float[3];

	@Override
	public void onSensorChanged(SensorEvent event) {
		double pitch;
		double yaw;
		double roll;

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			gravity = event.values;
		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			magnetic = event.values;

		if (gravity != null && magnetic != null) {
			if (SensorManager.getRotationMatrix(mR, mI, gravity, magnetic)) {
				SensorManager.getOrientation(mR, orientation);
				//yaw = orientation[0];
				pitch = Math.toDegrees(orientation[1]);
				roll = Math.toDegrees(orientation[2]);
				if (roll > 10 && roll < 40)
					changeDirection(Direction.RIGHT);
				else if (roll < -10 && roll > -45)
					changeDirection(Direction.LEFT);
				if (pitch > 15 && pitch < 45)
					changeDirection(Direction.UP);
				else if (pitch < -15 && pitch > -45)
					changeDirection(Direction.DOWN);
			}
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
}
