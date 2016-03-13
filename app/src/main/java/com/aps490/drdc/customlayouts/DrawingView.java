package com.aps490.drdc.customlayouts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
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

        //paint.setStyle(Paint.Style.FILL);
    }

    public DrawingView(Context context, AttributeSet attributeSet) {
        super(context);
        setZOrderOnTop(true);
        sh = getHolder();
        sh.addCallback(this);
        paint.setColor(Color.RED);
        sh.setFormat(PixelFormat.TRANSLUCENT);

        //paint.setStyle(Paint.Style.FILL);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        canvas.drawCircle(100, 200, 50, paint);
        sh.unlockCanvasAndPost(canvas);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}