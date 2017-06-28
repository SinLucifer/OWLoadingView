package com.sin.overwatchloading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class OverWatchLoadingView extends SurfaceView implements SurfaceHolder.Callback{
    public static final String TAG = OverWatchLoadingView.class.getSimpleName();

    public static final int STATE_SHOWING = 0;
    public static final int STATE_DISAPPEARING = 1;

    public static int state = STATE_SHOWING;

    public static final float SHOWING_SCALE = 0.6f;

    private Paint mPaint;
    private Canvas canvas;

    private int radius;
    private int color;
    private float space = 0;

    private Point[] centerPoint;
    private Hexagon[] hexagon;

    private float hexagonWidth = 0;

    private SurfaceHolder holder;

    private static boolean isFirst = true;
    private static boolean isLoop = false;

    private Thread drawThread;

    private Runnable animation = new Runnable() {
        @Override
        public void run() {
//            Log.i(TAG, "run: looping!");
            try {
                while (isLoop) {
                    draw();
                    flush();
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public OverWatchLoadingView(Context context) {
        this(context, null);
    }

    public OverWatchLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverWatchLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OverWatchLoadingView);

        color = a.getColor(R.styleable.OverWatchLoadingView_color, Color.BLACK);
        radius = a.getDimensionPixelSize(R.styleable.OverWatchLoadingView_radius, 8);

        a.recycle();

        hexagonWidth = (float)(Math.sqrt(3) * radius);
        space = radius / 10f;

        centerPoint = new Point[7];
        hexagon = new Hexagon[7];

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);

        holder = getHolder();
        setZOrderOnTop(true);
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
    }

    private void draw() {
        try {
            canvas = holder.lockCanvas();
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(mPaint);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
            if (isFirst) {
                calculateCP(getWidth() / 2 ,getHeight() / 2);
                createHexagon();
                isFirst = false;
            } else {
                for (int i = 0; i < 7; i++) {
                    mPaint.setAlpha(hexagon[i].getAlpha());
                    canvas.drawPath(hexagon[i].getHexagon(),mPaint);
                }
            }

            holder.unlockCanvasAndPost(canvas);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST) {
            width = (int)(hexagonWidth * 3 + space * 2);
        }

        height = width;
        setMeasuredDimension(width,height);
    }

    private void flush() {
        if (state == STATE_SHOWING) {
            hexagon[0].addAlpha();
            hexagon[0].addScale();

            for (int i = 0; i < 6; i++) {
                if (hexagon[i].getScale() >= SHOWING_SCALE) {
                    hexagon[i + 1].addAlpha();
                    hexagon[i + 1].addScale();
                }
            }

            if (hexagon[6].getScale() == 1f) {
                state = STATE_DISAPPEARING;
            }

        } else {
            hexagon[0].subAlpha();
            hexagon[0].subScale();

            for (int i = 0; i < 6; i++) {
                if (hexagon[i].getScale() <= 1f - SHOWING_SCALE) {
                    hexagon[i + 1].subAlpha();
                    hexagon[i + 1].subScale();
                }
            }

            if (hexagon[6].getScale() == 0f) {
                state = STATE_SHOWING;
            }
        }
    }

    private void calculateCP(int x, int y) {
        int xOffSet = (int)((hexagonWidth + space) / 2);
        int yOffSet = (int)((hexagonWidth + space) * Math.sqrt(3) / 2);

        centerPoint[0] = new Point(x - xOffSet, y - yOffSet);
        centerPoint[1] = new Point(x + xOffSet, y - yOffSet);
        centerPoint[2] = new Point(x + (int)(hexagonWidth + space), y);
        centerPoint[3] = new Point(x + xOffSet, y + yOffSet);
        centerPoint[4] = new Point(x - xOffSet, y + yOffSet);
        centerPoint[5] = new Point(x - (int)(hexagonWidth + space), y);
        centerPoint[6] = new Point(x,y);
    }

    private void createHexagon() {
        for (int i = 0; i < 7; i++) {
            hexagon[i] = new Hexagon(centerPoint[i]);
        }
    }

    public void start() {
        isLoop = true;
        if (drawThread == null)
            drawThread = new Thread(animation);

        drawThread.start();
    }

    public void stop() {
        isLoop = false;
        createHexagon();
        draw();
        drawThread.interrupt();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawThread.interrupt();
    }

    private class Hexagon {
        private float scale = 0;
        private int alpha = 0;

        private static final float scaleOffSet = 0.1f;
        private static final int alphaOffSet = 25;

        private Point center;
        private Path hexagon;

        private Hexagon(Point center) {
            this.center = center;
            hexagon = new Path();
            generateHexagon();
        }

        private void generateHexagon() {
            hexagon.reset();
            hexagon.moveTo(center.x, center.y - radius * scale);
            hexagon.lineTo(center.x + hexagonWidth * scale / 2
                    , center.y - radius * scale / 2);
            hexagon.lineTo(center.x + hexagonWidth * scale / 2
                    , center.y + radius * scale / 2);
            hexagon.lineTo(center.x, center.y + radius * scale);
            hexagon.lineTo(center.x - hexagonWidth * scale  / 2
                    , center.y + radius * scale / 2);
            hexagon.lineTo(center.x - hexagonWidth * scale  / 2
                    , center.y - radius * scale/ 2);
            hexagon.close();
        }

        private float getScale() {
            return scale;
        }

        public int getAlpha() {
            return alpha;
        }

        private void addScale() {
            if (scale == 1f)
                return;

            scale = (scale + scaleOffSet) >= 1f ? 1f : scale + scaleOffSet;
            generateHexagon();
        }

        private void addAlpha() {
            if (alpha == 255)
                return;

            alpha = (alpha + alphaOffSet) >= 255? 255 : alpha + alphaOffSet;
        }

        private void subScale() {
            if (scale == 0f)
                return;

            scale = (scale - scaleOffSet) <= 0f ? 0 : scale - scaleOffSet;
            generateHexagon();
        }

        private void subAlpha() {
            if (alpha == 0)
                return;

            alpha = (alpha - alphaOffSet) >= 0? 0 : alpha - alphaOffSet;
        }

        private Path getHexagon() {
            return hexagon;
        }
    }
}
