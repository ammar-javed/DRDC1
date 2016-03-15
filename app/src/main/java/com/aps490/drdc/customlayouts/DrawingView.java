package com.aps490.drdc.customlayouts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by JollyRancher on 16-03-12.
 */
public class DrawingView extends SurfaceView
                               implements SurfaceHolder.Callback {
    private SurfaceHolder sh;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DrawingView(Context context) {
        super(context);
        setZOrderOnTop(true);
        sh = getHolder();
        sh.addCallback(this);
        paint.setColor(Color.RED);
        sh.setFormat(PixelFormat.TRANSLUCENT);
    }

    public DrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setZOrderOnTop(true);
        sh = getHolder();
        sh.addCallback(this);
        paint.setColor(Color.RED);
        sh.setFormat(PixelFormat.TRANSLUCENT);

    }

    public void surfaceCreated(SurfaceHolder holder) {
//        Canvas canvas = sh.lockCanvas();
//        doDraw(canvas, 50, 100);
//        sh.unlockCanvasAndPost(canvas);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private void doDraw(Canvas canvas, float x, float y) {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        canvas.drawCircle(x, y, 50, paint);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_MOVE:
                Canvas canvas = sh.lockCanvas();
                doDraw(canvas, event.getX(), event.getY());
                sh.unlockCanvasAndPost(canvas);
                break;
            default:
                break;
        }

        return false;
    }
}