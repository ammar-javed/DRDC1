package com.aps490.drdc.prototype;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by JollyRancher on 16-03-12.
 */
class DrawingView extends SurfaceView implements SurfaceHolder.Callback {

    private final SurfaceHolder surfaceHolder;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DrawingView(Context context) {
        super(context);
        getHolder().addCallback(this);
        surfaceHolder = getHolder();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        // TODO Auto-generated constructor stub
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        surfaceHolder = getHolder();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        // TODO Auto-generated constructor stub
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        surfaceHolder = getHolder();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        // TODO Auto-generated constructor stub
    }

//    public DrawingView() {
//        super();
//        surfaceHolder = getHolder();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.FILL);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                canvas.drawCircle(event.getX(), event.getY(), 50, paint);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
        return false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }
}