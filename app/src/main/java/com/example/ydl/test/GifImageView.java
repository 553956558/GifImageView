package com.example.ydl.test;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by liuzhifeng on 2017/6/15.
 */
public class GifImageView extends ImageView implements View.OnClickListener {

    private Movie mMovie;
    private boolean isAutoPlay;
    private boolean isPlaying;
    private Bitmap mPlayBtn;
    private int mImageWidth;
    private int mImageHeight;
    private long mMovieStartTime;

    public GifImageView(Context context) {
        super(context);
    }

    public GifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.GifImageView);
        isAutoPlay = typedArray.getBoolean(R.styleable.GifImageView_auto_play, false);
        typedArray.recycle();

        int resId = getResourceId(attrs);
        if (resId != 0) {
            InputStream in = context.getResources().openRawResource(resId);
            mMovie = Movie.decodeStream(in);
            if (mMovie != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                mImageWidth = bitmap.getWidth();
                mImageHeight = bitmap.getHeight();
                bitmap.recycle();

                if (!isAutoPlay) {
                    mPlayBtn = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                    setOnClickListener(this);
                }
            }
        }
    }

    /**
     * 读取image的src
     *
     * @param attrs
     * @return
     */
    private int getResourceId(AttributeSet attrs) {

        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            if (attrs.getAttributeName(i).equals("src")) {
                return attrs.getAttributeResourceValue(i, 0);
            }
        }

        return 0;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == getId()) {
            isPlaying = true;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mMovie != null) {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 开始播放GIF动画，播放完成返回true，未完成返回false。
     *
     * @param canvas
     */
    private boolean playGif(Canvas canvas) {
        long now = SystemClock.uptimeMillis();
        if (mMovieStartTime == 0) {
            mMovieStartTime = now;
        }
        int duration = mMovie.duration();
        if (duration == 0) {
            duration = 1000;
        }
        int relTime = (int) ((now - mMovieStartTime) % duration);
        mMovie.setTime(relTime);
        mMovie.draw(canvas, 0, 0);
        if ((now - mMovieStartTime) >= duration) {
            mMovieStartTime = 0;
            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie == null) {
            super.onDraw(canvas);
            return;
        }

        if (isAutoPlay) {
            playGif(canvas);
            invalidate();
        } else {
            if (isPlaying) {
                if (playGif(canvas)) {
                    isAutoPlay = false;
                }
                invalidate();
            } else {
                mMovie.setTime(0);
                mMovie.draw(canvas, 0, 0);

                int offsetX = mImageWidth / 2 - mPlayBtn.getWidth() / 2;
                int offsetY = mImageHeight / 2 - mPlayBtn.getHeight() / 2;
                canvas.drawBitmap(mPlayBtn, offsetX, offsetY, null);
            }
        }


    }
}
