package com.ralleq.influx.drawing;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.ralleq.influx.assets.SizeAssets;

public class ScreenHandler {

    private DrawRunnable drawRunnable;
    private BoundsRunnable boundsRunnable;
    private MotionRunnable motionRunnable;

    private DisplayMetrics displayMetrics;
    private float dpi;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceHolder.Callback surfaceHolderCallback;
    private Canvas canvas;
    private Thread drawThread;
    private boolean drawLoopIsRunning = true;

    public ScreenHandler(Activity activity, DrawRunnable drawRunnable, BoundsRunnable boundsRunnable, MotionRunnable motionRunnable) {
        this.drawRunnable = drawRunnable;
        this.boundsRunnable = boundsRunnable;
        this.motionRunnable = motionRunnable;
        initializeDisplayMetrics(activity);
        initializeSurface(activity);
        initializeThread();
        startThread();
    }
    private void initializeDisplayMetrics(Context context) {
        displayMetrics = context.getResources().getDisplayMetrics();
        dpi = (displayMetrics.xdpi + displayMetrics.ydpi)/2.0f;
        SizeAssets.updateDPI(dpi);
    }
    private void initializeSurface(Context context) {
        surfaceView = new SurfaceView(context) {
            @Override
            public boolean onTouchEvent(MotionEvent motionEvent) {
                motionRunnable.run(motionEvent);
                return true;
            }
        };
        surfaceHolderCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                boundsRunnable.run(0, 0, width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        };
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(surfaceHolderCallback);
    }
    private void initializeThread() {
        drawThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(drawLoopIsRunning) {
                    draw();
                }
            }
        });
        drawThread.setName("Draw Thread");
    }
    private void startThread() {
        drawThread.start();
    }

    public void draw() {
        canvas = null;
        try {
            if(surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas();
            }
            if(canvas != null) {
                drawRunnable.run(canvas);
            }
        } finally {
            if(canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void hideUI() {
        surfaceView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }
}
