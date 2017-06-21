package com.sin.overwatchloading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class OverWatchLoadingView extends View {
    public static final String TAG = OverWatchLoadingView.class.getSimpleName();

    public static final int STATE_SHOWING = 0;
    public static final int STATE_DISAPPEARING = 1;

    public static int state = STATE_SHOWING;

    public static final float SHOWING_SCALE = 0.6f;

    private Paint mPaint;

    private int radius;
    private int color;
    private float space = 0;

    private Point[] centerPoint;
    private Hexagon[] hexagon;

    private float hexagonWidth = 0;

    private static boolean isFirst = true;
    private static boolean isLoop = false;

    private Runnable animation = new Runnable() {
        @Override
        public void run() {
//            Log.i(TAG, "run: looping!");
            flush();
            invalidate();
            if(isLoop) {
                postDelayed(animation,10);
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

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
        post(animation);
    }

    public void stop() {
        isLoop = false;
    }

    private class Hexagon {
        private float scale = 0;
        private int alpha = 0;

        private static final float scaleOffSet = 0.1f;
        private static final int alphaOffSet = 25;

        private Point center;
        private Path hexagon;

        public Hexagon(Point center) {
            this.center = center;
            hexagon = new Path();
            generateHexagon();
        }

        public void generateHexagon() {
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

        public float getScale() {
            return scale;
        }

        public int getAlpha() {
            return alpha;
        }

        public void addScale() {
            if (scale == 1f)
                return;

            scale = (scale + scaleOffSet) >= 1f ? 1f : scale + scaleOffSet;
            generateHexagon();
        }

        public void addAlpha() {
            if (alpha == 255)
                return;

            alpha = (alpha + alphaOffSet) >= 255? 255 : alpha + alphaOffSet;
        }

        public void subScale() {
            if (scale == 0f)
                return;

            scale = (scale - scaleOffSet) <= 0f ? 0 : scale - scaleOffSet;
            generateHexagon();
        }

        public void subAlpha() {
            if (alpha == 0)
                return;

            alpha = (alpha - alphaOffSet) >= 0? 0 : alpha - alphaOffSet;
        }

        public Path getHexagon() {
            return hexagon;
        }
    }
}
