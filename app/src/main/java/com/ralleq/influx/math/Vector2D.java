package com.ralleq.influx.math;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.util.Vector;

public class Vector2D {

    float x = 0, y = 0;

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public Vector2D() {

    }

    public void add(float xAdd, float yAdd) {
        x += xAdd;
        y += yAdd;
    }
    public static Vector2D add(Vector2D first, Vector2D second) {
        return new Vector2D(first.getX() + second.getX(), first.getY() + second.getY());
    }
    public void subtract(float xSubtract, float ySubtract) {
        x -= xSubtract;
        y -= ySubtract;
    }
    public static Vector2D subtract(Vector2D first, Vector2D second) {
        return new Vector2D(first.getX() - second.getX(), first.getY() - second.getY());
    }

    public float getLength() {
        return (float) Math.sqrt(x*x + y*y);
    }
    public void scaleLength(float scalar) {
        x *= scalar;
        y *= scalar;
    }
    public static Vector2D scaleLength(Vector2D vector, float scalar) {
        return new Vector2D(vector.getX() * scalar, vector.getY() * scalar);
    }
    public void forceLength(float newLength) {
        float scalar = newLength/getLength();
        scaleLength(scalar);
    }
    public static void forceLength(Vector2D vector, float newLength) {
        float scalar = newLength/vector.getLength();
        Vector2D.scaleLength(vector, scalar);
    }

    public void invert() {
        x = -x;
        y = -y;
    }
    public static Vector2D invert(Vector2D vector) {
        return new Vector2D(-vector.getX(), -vector.getY());
    }
    public void absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
    }
    public static Vector2D absolute(Vector2D vector) {
        return new Vector2D(Math.abs(vector.getX()), Math.abs(vector.getY()));
    }
    public float getAbsoluteX() { return Math.abs(x); }
    public float getAbsoluteY() { return Math.abs(y); }

    public float dotProduct(float xOther, float yOther) {
        return getX()*xOther + getY()*yOther;
    }
    public static float dotProduct(Vector2D first, Vector2D second) {
        return first.getX()*second.getX() + first.getY()*second.getY();
    }
    public float determinant(float xOther, float yOther) {
        return getX()*yOther - getY()*xOther;
    }
    public static float determinant(Vector2D first, Vector2D second) {
        return first.getX()*second.getY() - first.getY()*second.getX();
    }

    public boolean pointIsInsideRect(RectF rect) {
        return rect.contains(x, y);
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void replicate(Vector2D toReplicate) {
        this.x = toReplicate.getX();
        this.y = toReplicate.getY();
    }

    @NonNull
    @Override
    public String toString() {
        return  "[" + x + ", " + y + "]";
    }
}
