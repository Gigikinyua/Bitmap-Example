package com.jemimah.bitmapexample;

/**
 * Requires improvement on performances, check frame -rates
 * uses example bitmaps - implement actual view bitmaps
 * currently requires an arraylist of 5 , - handle a scenario where the items are less
 *
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class AnimationView extends View {
    private static final String TAG = "AnimationView";

    Paint paint;

    ArrayList<Bitmap> icons = new ArrayList<>();
    int bm_offsetX, bm_offsetY, screenWidth, screenHeight;

    Path animPath;
    PathMeasure pathMeasure;
    float pathLength;

    float step;   //distance each step
    float distance;  //distance moved


    // new buffers
    float[] iterator = new float[7];
    float[] iteratorUpper = new float[7];
    ArrayList<float[]> positions = new ArrayList();
    ArrayList<float[]> tangents = new ArrayList();

    Boolean isForward = false;


    boolean swipe = false;
    boolean initialized = true;
    boolean offsetLeft = true;
    boolean offsetRight = true;

    GestureDetector gestureDetector;

    Matrix matrix, inverseMatrix;

    LinearLayout container;
    Context context;
    Canvas mcanvas;

    public AnimationView(Context context) {
        super(context);
        initMyView();
    }

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMyView();
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMyView();
    }

    public AnimationView(Context context, LinearLayout container) {
        super(context);
        this.context = context;
        this.container = container;
        gestureDetector = new GestureDetector(context, new GestureListener());
        initMyView();
    }

    public void initMyView() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);

        /**
         * this would be dynamic
         */

        icons.add(BitmapFactory.decodeResource(getResources(), R.drawable.woman));
        icons.add(BitmapFactory.decodeResource(getResources(), R.drawable.woman));
        icons.add(BitmapFactory.decodeResource(getResources(), R.drawable.woman));
        icons.add(BitmapFactory.decodeResource(getResources(), R.drawable.woman));
        icons.add(BitmapFactory.decodeResource(getResources(), R.drawable.woman));


        for (int i = 0; i < 5; i++) {
            positions.add(new float[2]);
            tangents.add(new float[2]);
        }


        bm_offsetX = icons.get(0).getWidth() / 2;
        bm_offsetY = icons.get(0).getHeight() / 2;

        animPath = new Path();

        step = 24;
        distance = 0;
        iterator[0] = 300;

        matrix = new Matrix();
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        mcanvas = canvas;
        screenHeight = container.getHeight();
        screenWidth = container.getWidth();

        drawAnimationPathLine();
        pathMeasure = new PathMeasure(animPath, false);
        pathLength = pathMeasure.getLength();
        mcanvas.drawPath(animPath, paint);

        initializeAnimationBounds();

        initialized = false;

        if (!swipe) {

            drawInitialLocations();

        } else {
            if (isForward) {
                rightShiftIcons();
                animateForwardViews();
            } else {
                leftShiftIcons();
                animateBackViews();
            }
            invalidate();

        }


    }

    void rightShiftIcons() {
        Bitmap first = icons.get(icons.size() - 1);


        for (int i = icons.size() - 1; i > 1; i--) {
            icons.set(i, icons.get(i - 1));
        }

        icons.set(0, first);
    }

    void leftShiftIcons() {
        Bitmap last = icons.get(0);

        Bitmap cache;
        for (int i = 0; i < icons.size() - 1; i++) {
            cache = icons.get(i);
            icons.set(i, icons.get(i + 1));
        }
        icons.set(icons.size() - 1, last);
    }

    /**
     * initialize bounds
     */

    void initializeAnimationBounds() {
        float startPoint = pathLength / 7;

        for (int i = 0; i < 6; i++) {
            iteratorUpper[i] = (i + 1) * startPoint;
            if (i == 6) {
                iteratorUpper[i] = pathLength;
            }

            if (initialized) {
                iterator[i] = iteratorUpper[i];
            }
        }

    }

    void offsetRightBounds() {
        float startPoint = pathLength / 8;

        for (int i = 0; i < 6; i++) {
            iteratorUpper[i] = (i + 2) * startPoint;

            if (initialized) {
                iterator[i] = iteratorUpper[i];
            }
        }

        offsetRight=false;
    }

    void offsetLeftBounds() {

        float startPoint = pathLength / 8;

        for (int i = 0; i < 6; i++) {
            iteratorUpper[i] = (i + 1) * startPoint;


            if (offsetLeft) {

                iterator[i] = (i) * startPoint;
            }


        }
        offsetLeft = false;

    }


    /**
     * draw the animation path line
     */
    void drawAnimationPathLine() {
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(9);
        animPath.moveTo(0, (float) (screenHeight * 0.9857));
        animPath.cubicTo(
                (float) (screenWidth * 1.7), (float) (screenHeight * 0.95),
                (float) (screenWidth * 0.0714), (float) (screenHeight * 0.6992),
                (float) (screenWidth * 0.6142), (float) (screenHeight * 0.5833)
        );

        animPath.cubicTo(
                (float) (screenWidth * 1.4), (float) (screenHeight * 0.4167),
                (float) (screenWidth * 0.3), (float) (screenHeight * 0.3333),
                screenWidth, (float) (screenHeight * 0.185)
        );

    }

    /**
     * draw initial icon view locations
     */
    void drawInitialLocations() {
        for (int m = 0; m < 5; m++) {
            mcanvas.drawBitmap(
                    icons.get(m),
                    getInitialMatrix(iterator[m],
                            positions.get(m),
                            tangents.get(m)),
                    null);
        }
    }

    void animateForwardViews() {
        offsetRightBounds();
        for (int m = 4; m >= 0; m--) {
            if (m == 0) {
                if (iterator[m] > (pathLength / 9)) {
                    drawIconView(m, true);
                } else {
                    mcanvas.drawBitmap(icons.get(m), getInitialMatrix(iterator[m], positions.get(m), tangents.get(m)), null);
                }
            } else {

                if (iterator[m] > iteratorUpper[m - 1]) {
                    drawIconView(m, true);
                } else {
                    mcanvas.drawBitmap(icons.get(m), getInitialMatrix(iterator[m], positions.get(m), tangents.get(m)), null);

                }
            }
        }

    }

    /**
     * animate the views when there is a swipe = true
     */
    void animateBackViews() {
        offsetLeftBounds();

        for (int m = 0; m < 5; m++) {
            if (iterator[m] < iteratorUpper[m]) {
                drawIconView(m, false);
            } else {
                mcanvas.drawBitmap(icons.get(m), getInitialMatrix(iterator[m], positions.get(m), tangents.get(m)), null);
            }
        }
    }

    void drawIconView(int index, Boolean isForward) {
        pathMeasure.getPosTan(iterator[index], positions.get(index), tangents.get(0));
        matrix.reset();
        float degrees = (float) (Math.atan2(tangents.get(index)[1], tangents.get(index)[0]) * 180.0 / Math.PI);
        matrix.postRotate(degrees, bm_offsetX, bm_offsetY);
        matrix.setTranslate(positions.get(index)[0] - bm_offsetX, positions.get(index)[1] - bm_offsetY);

        mcanvas.drawBitmap(icons.get(index), matrix, null);
        if (isForward) {
            iterator[index] -= step;
        } else {
            iterator[index] += step;
        }
    }


    Matrix getInitialMatrix(float v, float[] pos, float[] tan) {
        Matrix matrix = new Matrix();
        pathMeasure.getPosTan(v, pos, tan);

        matrix.reset();
        float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
        matrix.postRotate(degrees, bm_offsetX, bm_offsetY);
        matrix.setTranslate(pos[0] - bm_offsetX, pos[1] - bm_offsetY);
        return matrix;
    }

    void forward() {
        offsetRight = true;
        isForward = true;
        Toast.makeText(context, "swiped forward", Toast.LENGTH_SHORT).show();
        this.postInvalidate();
    }

    void backward() {
        offsetLeft = true;
        isForward = false;
        Toast.makeText(context, "swiped back", Toast.LENGTH_SHORT).show();
        this.postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        return true;
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        private View _touchingView;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    void onSwipeRight() {
        Log.d(TAG, "onSwipeRight: Move backwards");
        swipe = true;
        initialized = true;
        backward();
    }

    void onSwipeLeft() {
        Log.d(TAG, "onSwipeLeft: Move forward");
        swipe = true;
        initialized = true;
        forward();
    }

    void onSwipeTop() {
        Log.d(TAG, "onSwipeTop: Move backward");
        swipe = true;
        initialized = true;
        backward();
    }

    void onSwipeBottom() {
        Log.d(TAG, "onSwipeBottom: Move forward");
        swipe = true;
        initialized = true;
        forward();
    }

}
