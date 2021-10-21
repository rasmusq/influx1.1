package com.ralleq.influx.touch;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.ralleq.influx.assets.SizeAssets;

import java.util.Random;

public class TouchHandler {

    private TouchPointer[] touchPointers;
    private static final int MAX_POINTERS = 10;

    private TouchRunnable touchRunnable;

    private Paint paint;
    private Random random;
    private float paintStrokeWidth = ;
    public void drawTouchPointers(Canvas canvas) {

        for(int i = 0; i < touchPointers.length; i++) {
            canvas.drawCircle(
                    touchPointers[i].getPosition().getX(), touchPointers[i].getPosition().getY());
        }
    }

    public TouchHandler(TouchRunnable touchRunnable) {
        this.touchRunnable = touchRunnable;
        initializeTouchPointers();
        initializeDrawingObjects();
    }
    public void initializeTouchPointers() {
        touchPointers = new TouchPointer[MAX_POINTERS];
        for(int i = 0; i < touchPointers.length; i++) {
            touchPointers[i] = new TouchPointer();
        }
    }
    public void initializeDrawingObjects() {
        paint = new Paint();
        random = new Random();
    }

    public void translateMotionEvent(MotionEvent motionEvent) {
        //Get event information
        int action = motionEvent.getActionMasked();
        int pointerIndex = motionEvent.getActionIndex();
        int pointerId = motionEvent.getPointerId(pointerIndex);

        //Update active pointer
        if(pointerId < touchPointers.length) {
            boolean pointerActionIsDown =
                    action == MotionEvent.ACTION_POINTER_DOWN ||
                            action == MotionEvent.ACTION_DOWN;
            boolean pointerActionIsUp =
                    action == MotionEvent.ACTION_POINTER_UP ||
                            action == MotionEvent.ACTION_UP;
            boolean pointerActionIsMove =
                    action == MotionEvent.ACTION_MOVE;
            if(pointerActionIsDown) {
                touchPointers[pointerId].setDown(true);
            } else if(pointerActionIsUp) {
                touchPointers[pointerId].setDown(false);
            }
            if(pointerActionIsDown || pointerActionIsUp || pointerActionIsMove) {
                updatePointerCoordinates(pointerId, motionEvent);
            }
        }
        TouchEvent touchEvent = new TouchEvent(touchPointers, pointerId);
        touchRunnable.run(touchEvent);
    }

    private void updatePointerCoordinates(int pointerId, MotionEvent motionEvent) {
        TouchPointer touchPointer = touchPointers[pointerId];
        float newX = motionEvent.getX(),
                newY = motionEvent.getY(),
                oldX = touchPointer.getPosition().getX(),
                oldY = touchPointer.getPosition().getY();
        float velocityX = newX-oldX,
                velocityY = newY-oldY;

        touchPointer.getVelocity().set(velocityX, velocityY);
        touchPointer.getPosition().set(newX, newY);
        touchPointer.getPreviousPosition().set(oldX, oldY);
        touchPointer.addVelocityToTravelDistance();
    }

}
