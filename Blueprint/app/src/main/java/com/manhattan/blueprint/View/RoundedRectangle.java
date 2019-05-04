package com.manhattan.blueprint.View;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.takusemba.spotlight.shape.Shape;

public class RoundedRectangle implements Shape {

    private float width;
    private float height;
    private float leftOffset;
    private float topOffset;

    public RoundedRectangle(float leftOffset, float topOffset, float width, float height) {
        this.leftOffset = leftOffset;
        this.topOffset = topOffset;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getHeight() {
        return (int) height;
    }

    @Override
    public int getWidth() {
        return (int) width;
    }

    @Override
    public void draw(Canvas canvas, PointF point, float value, Paint paint) {
        canvas.drawRoundRect(this.leftOffset, this.topOffset, this.leftOffset + this.width, this.topOffset + this.height, 40, 40, paint);
    }

}
