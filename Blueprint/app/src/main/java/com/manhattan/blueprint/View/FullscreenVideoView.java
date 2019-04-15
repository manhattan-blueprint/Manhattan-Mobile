package com.manhattan.blueprint.View;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class FullscreenVideoView extends VideoView {
    private int videoWidth;
    private int videoHeight;

    public FullscreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullscreenVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FullscreenVideoView(Context context) {
        super(context);
    }

    public void setVideoSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);
        if (videoWidth > 0 && videoHeight > 0) {
            if (videoWidth * height < width * height) {
                // Video too tall
                height = width * videoHeight / videoWidth;
            } else if (videoWidth * height > width * videoHeight) {
                // Video too wide
                width = height * videoWidth / videoHeight;
            }
        }
        setMeasuredDimension(width, height);
    }
}
