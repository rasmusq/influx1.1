package com.ralleq.influx;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.ralleq.influx.audio.AudioRouter;
import com.ralleq.influx.audio.AudioRunnable;
import com.ralleq.influx.drawing.BoundsRunnable;
import com.ralleq.influx.drawing.ScreenHandler;
import com.ralleq.influx.drawing.DrawRunnable;
import com.ralleq.influx.drawing.MotionRunnable;
import com.ralleq.influx.touch.TouchEvent;
import com.ralleq.influx.touch.TouchHandler;
import com.ralleq.influx.touch.TouchRunnable;

public class MainActivity extends Activity {

    private AudioRouter audioRouter;
    private TouchHandler touchHandler;
    private ScreenHandler screenHandler;

    double wavetime = 0.0;
    double wavespeed = 0.0;
    double time = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioRouter = new AudioRouter(this, new AudioRunnable() {
            @Override
            public void run(int[] leftInputSamples, int[] rightInputSamples, int[] leftOutputSamples, int[] rightOutputSamples, int currentSampleIndex) {
                leftOutputSamples[currentSampleIndex] = (int) (Short.MAX_VALUE * Math.sin(wavetime));
                rightOutputSamples[currentSampleIndex] = leftOutputSamples[currentSampleIndex];
                wavetime += wavespeed/10;
                wavespeed = Math.sin(time)*0.7 ;
                time += 0.0001;
            }
        });
        touchHandler = new TouchHandler(new TouchRunnable() {
            @Override
            public void run(TouchEvent touchEvent) {

            }
        });
        screenHandler = new ScreenHandler(this,
            new DrawRunnable() {
                @Override
                public void run(Canvas canvas) {
                    canvas.drawColor(Color.RED);
                    touchHandler.drawTouchPointers(canvas);
                }
            }, new BoundsRunnable() {
                @Override
                public void run(float left, float top, float right, float bottom) {
                    touchHandler.updateBounds(left, top, right, bottom);
                }
            }, new MotionRunnable() {
                @Override
                public void run(MotionEvent motionEvent) {
                    touchHandler.translateMotionEvent(motionEvent);
                }
            }
        );
        hideAllUI();
        setContentView(screenHandler.getSurfaceView());
    }
    private void hideAllUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        screenHandler.hideUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioRouter.stopNativeAudioEngine();
    }
    @Override
    protected void onResume() {
        super.onResume();
        screenHandler.hideUI();
    }

}
