package com.ralleq.influx.touch;

import com.ralleq.influx.math.Vector2D;

import java.util.Vector;

public class TouchPointer {

    private Vector2D position, previousPosition, velocity,
            lastDownPosition, lastUpPosition;
    private boolean down;
    private long lastDownTime, lastUpTime;
    private float travelDistance;

    public TouchPointer() {
        position = new Vector2D();
        previousPosition = new Vector2D();
        velocity = new Vector2D();
        lastDownPosition = new Vector2D();
        lastUpPosition = new Vector2D();
    }

    public void setDown(boolean down) {
        long timeNow = System.nanoTime();
        if(!this.down && down) {
            lastDownPosition.replicate(position);
            lastDownTime = timeNow;
            travelDistance = 0;
        } else if(this.down && !down) {
            lastUpTime = timeNow;
            lastUpPosition.replicate(position);
        }
        this.down = down;
    }
    public long getDownDuration() {
        if(lastUpTime > lastDownTime) {
            return lastUpTime - lastDownTime;
        } else {
            long timeNow = System.nanoTime();
            return timeNow - lastDownTime;
        }
    }

    public void changePosition(float xNew, float yNew) {
        previousPosition.replicate(position);
        position.set(xNew, yNew);
        velocity.set(position.getX() - previousPosition.getX(),
                position.getY() - previousPosition.getY());

    }

    public void addVelocityToTravelDistance() {
        travelDistance += Math.sqrt(
                velocity.getAbsoluteX()*velocity.getAbsoluteX() +
                        velocity.getAbsoluteY()*velocity.getAbsoluteY());
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getPreviousPosition() {
        return previousPosition;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public Vector2D getLastDownPosition() {
        return lastDownPosition;
    }

    public Vector2D getLastUpPosition() {
        return lastUpPosition;
    }

    public boolean isDown() {
        return down;
    }

    public long getLastDownTime() {
        return lastDownTime;
    }

    public long getLastUpTime() {
        return lastUpTime;
    }

    public float getTravelDistance() {
        return travelDistance;
    }

}
