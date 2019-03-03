package com.manhattan.blueprint.View;

import android.util.Log;
import android.view.ScaleGestureDetector;

import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.android.gestures.StandardScaleGestureDetector;

public class MapGestureListener implements MoveGestureDetector.OnMoveGestureListener, StandardScaleGestureDetector.StandardOnScaleGestureListener {

    public interface GestureDelegate {
        void panBy(float amount);
        void scaleBy(float amount);
    }

    private GestureDelegate delegate;
    public MapGestureListener(GestureDelegate delegate){
        this.delegate = delegate;
    }

    // region ScaleListener
    @Override
    public boolean onScaleBegin(StandardScaleGestureDetector detector) {
        return true;
    }

    @Override
    public boolean onScale(StandardScaleGestureDetector detector) {
        delegate.scaleBy(detector.getScaleFactor());
        return false;
    }

    @Override
    public void onScaleEnd(StandardScaleGestureDetector detector, float velocityX, float velocityY) {

    }
    // endregion


    // region MoveListener
    @Override
    public boolean onMoveBegin(MoveGestureDetector detector) {
        return true;
    }

    @Override
    public boolean onMove(MoveGestureDetector detector, float distanceX, float distanceY) {
        // Make it harder to pan than the amount they actually move their finger
        float dragFactor = 0.1f;
        delegate.panBy(distanceX * dragFactor);
        return false;
    }

    @Override
    public void onMoveEnd(MoveGestureDetector detector, float velocityX, float velocityY) {  }
    // endregion
}
