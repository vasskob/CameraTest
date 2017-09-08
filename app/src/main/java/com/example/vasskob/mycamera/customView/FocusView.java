package com.example.vasskob.mycamera.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.vasskob.mycamera.R;

import static com.example.vasskob.mycamera.utils.Constants.FOCUS_VIEW_HEIGHT;


public class FocusView extends View {

    private static final String TAG = FocusView.class.getSimpleName();
    public static final int RADIUS = 120;
    public static final int STROKE_WIDTH = 4;
    private boolean haveTouch = false;
    private Rect touchArea;
    private Paint paint;

    public FocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.colorWhite));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        haveTouch = false;
    }

    public void setHaveTouch(boolean val, Rect rect) {
        Log.d(TAG, "setHaveTouch: ");
        haveTouch = val;
        touchArea = rect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: d");
        if (haveTouch) {
            drawCircleFocus(canvas);
        }
    }

    private void drawCircleFocus(Canvas canvas) {
        canvas.drawCircle(touchArea.left + FOCUS_VIEW_HEIGHT, touchArea.top + FOCUS_VIEW_HEIGHT, RADIUS, paint);
    }

    private void drawRectFocus(Canvas canvas) {
        canvas.drawRect(touchArea.left, touchArea.top, touchArea.right, touchArea.bottom, paint);
    }
}
