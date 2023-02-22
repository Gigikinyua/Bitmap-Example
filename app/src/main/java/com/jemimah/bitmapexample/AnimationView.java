package com.jemimah.bitmapexample;

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

public class AnimationView extends View {
    private static final String TAG = "AnimationView";

    Paint paint;

    Bitmap bm;
    int bm_offsetX, bm_offsetY, screenWidth, screenHeight;

    Path animPath;
    PathMeasure pathMeasure;
    float pathLength;

    float step;   //distance each step
    float distance;  //distance moved

    float[] pos;
    float[] tan;

    int bitmapIndex=0;

    boolean swipe = false;

    GestureDetector gestureDetector;

    Matrix matrix, inverseMatrix;

    LinearLayout container;
    Context context;

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

        bm = BitmapFactory.decodeResource(getResources(), R.drawable.woman);
        bm_offsetX = bm.getWidth() / 2;
        bm_offsetY = bm.getHeight() / 2;

        animPath = new Path();

        step = 12;
        distance = 0;
        pos = new float[2];
        tan = new float[2];

        matrix = new Matrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        screenHeight = container.getHeight();
        screenWidth = container.getWidth();

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

        pathMeasure = new PathMeasure(animPath, false);
        pathLength = pathMeasure.getLength();

        canvas.drawPath(animPath, paint);

        float interval = pathLength / 3;



        float pos2 = distance + interval;
        float pos3 = pos2 + interval;


        for ( int j=0; j < 3; j++) {
            bitmapIndex =j;

            if (!swipe) {

                drawBitmap(j, distance, pos2, pos3, canvas);
            } else {

                float endpoint;
                if (j == 0) {
                    endpoint = 0 + interval;
                    if (distance <= endpoint) {
                        drawBitmap(j, distance, pos2, pos3, canvas);

                        distance += step;
                    } else {
                        drawBitmap(j, distance, pos2, pos3, canvas);
                        swipe= false;
                    }
                } else {
                    drawBitmap(j, distance, pos2, pos3, canvas);
                }
            }

        }


        invalidate();



    }

    public void drawBitmap(int view, float pos1, float pos2, float pos3, Canvas canvas) {

        if (view == 0) {
            pathMeasure.getPosTan(pos1, pos, tan);

            matrix.reset();
            float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
            matrix.postRotate(degrees, bm_offsetX, bm_offsetY);
            matrix.setTranslate(pos[0] - bm_offsetX, pos[1] - bm_offsetY);

            canvas.drawBitmap(bm, matrix, null);
        } else if (view == 1){
            pathMeasure.getPosTan(pos2, pos, tan);

            matrix.reset();
            float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
            matrix.postRotate(degrees, bm_offsetX, bm_offsetY);
            matrix.setTranslate(pos[0] - bm_offsetX, pos[1] - bm_offsetY);

            canvas.drawBitmap(bm, matrix, null);
        } else if (view == 2){
            pathMeasure.getPosTan(pos3, pos, tan);

            matrix.reset();
            float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
            matrix.postRotate(degrees, bm_offsetX, bm_offsetY);
            matrix.setTranslate(pos[0] - bm_offsetX, pos[1] - bm_offsetY);

            canvas.drawBitmap(bm, matrix, null);
        }
    }

    void forward() {
        distance=0;
        Toast.makeText(context, "swiped forward", Toast.LENGTH_SHORT).show();
        this.postInvalidate();
    }

    void backward() {
        distance=0;
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
        backward();
    }

    void onSwipeLeft() {
        Log.d(TAG, "onSwipeLeft: Move forward");
        swipe = true;
        forward();
    }

    void onSwipeTop() {
        Log.d(TAG, "onSwipeTop: Move backward");
        swipe = true;
        backward();
    }

    void onSwipeBottom() {
        Log.d(TAG, "onSwipeBottom: Move forward");
        swipe = true;
        forward();
    }

    static class CustomViewPos {
        float position;

        public CustomViewPos(float position) {
            this.position = position;
        }
    }

}
