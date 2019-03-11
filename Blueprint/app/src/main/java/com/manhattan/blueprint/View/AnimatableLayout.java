package com.manhattan.blueprint.View;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class AnimatableLayout extends RelativeLayout {
    private float destX;
    private float destY;

    public AnimatableLayout(Context context) {
        super(context);
    }

    public AnimatableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnimatableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setDestination(float destX, float destY) {
        this.destX = destX;
        this.destY = destY;
    }

    public float getDestX() {
        return destX;
    }

    public float getDestY() {
        return destY;
    }
}
