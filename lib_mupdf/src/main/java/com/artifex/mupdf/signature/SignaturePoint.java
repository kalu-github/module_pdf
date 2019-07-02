package com.artifex.mupdf.signature;

/**
 * description: 手写轨迹, 点
 * create by kalu on 2019/07/01
 */
public class SignaturePoint {

    public final float x;
    public final float y;
    public final long timestamp;

    public SignaturePoint(float x, float y) {
        this.x = x;
        this.y = y;
        this.timestamp = System.currentTimeMillis();
    }

    public float velocityFrom(SignaturePoint start) {
        float velocity = distanceTo(start) / (this.timestamp - start.timestamp);
        if (velocity != velocity)
            return 0f;
        return velocity;
    }

    public float distanceTo(SignaturePoint point) {
        return (float) Math.sqrt(Math.pow(point.x - this.x, 2) + Math.pow(point.y - this.y, 2));
    }
}