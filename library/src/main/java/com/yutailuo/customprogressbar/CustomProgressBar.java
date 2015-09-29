package com.yutailuo.customprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import yutailuo.library.R;

public class CustomProgressBar extends View {

    private int mProgress = 0;

    private float mIndicatortHeight;

    private float mIndicatorWidth;

    private int mReachedBarColor;

    private int mUnreachedBarColor;

    private float mBarThickness;

    private int mTextColor;

    private float mTextSize;

    private float mTextOffset;

    private int mTextVisibility;

    private IndicatorType mIndicatorType;

    private Paint mProgressPaint;

    private Paint mTextPaint;

    private Paint mIndicatorPaint;

    private ProgressBarListener mListener;

    public enum IndicatorType {
        Line, Circle, Square
    }

    private final int DEFAULT_REACHED_COLOR = Color.rgb(66, 145, 241);
    private final int DEFAULT_UNREACHED_COLOR = Color.rgb(204, 204, 204);
    private final float DEFAULT_TEXT_SIZE = sp2px(10.0f);
    private final float DEFAULT_BAR_THICKNESS = dp2px(1.5f);
    private final float DEFAULT_INDICATOR_HEIGHT = dp2px(10.0f);
    private final float DEFAULT_INDICATOR_WIDTH = dp2px(3.0f);
    private final float DEFAULT_TEXT_OFFSET = dp2px(3.0f);
    private final int DEFAULT_TEXT_VISIBLE = VISIBLE;
    private final int DEFAULT_INDICATOR_TYPE = 0;


    private static final String SAVED_STATE = "saved_state";
    private static final String TEXT_COLOR = "text_color";
    private static final String TEXT_SIZE = "text_size";
    private static final String TEXT_OFFSET = "text_offset";
    private static final String TEXT_VISIBILITY = "text_visibility";
    private static final String REACHED_BAR_COLOR = "reached_bar_color";
    private static final String UNREACHED_BAR_COLOR = "unreached_bar_color";
    private static final String BAR_THICKNESS = "bar_thickness";
    private static final String CURRENT_PROGRESS = "progress";
    private static final String INDICATOR_WIDTH = "indicator_width";
    private static final String INDICATOR_HEIGHT = "indicator_height";

    public CustomProgressBar(Context context) {
        this(context, null);
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        final TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs,
                R.styleable.CustomProgressBar, 0, 0);

        setProgress(typedArray.getInt(R.styleable.CustomProgressBar_current_progress, 0));

        mIndicatortHeight = typedArray.getDimension(R.styleable.CustomProgressBar_indicator_height, DEFAULT_INDICATOR_HEIGHT);
        mIndicatorWidth = typedArray.getDimension(R.styleable.CustomProgressBar_indicator_width, DEFAULT_INDICATOR_WIDTH);

        mReachedBarColor = typedArray.getColor(R.styleable.CustomProgressBar_reached_color, DEFAULT_REACHED_COLOR);
        mUnreachedBarColor = typedArray.getColor(R.styleable.CustomProgressBar_unreached_color, DEFAULT_UNREACHED_COLOR);
        mBarThickness = typedArray.getDimension(R.styleable.CustomProgressBar_bar_thickness, DEFAULT_BAR_THICKNESS);

        mTextColor = typedArray.getColor(R.styleable.CustomProgressBar_text_color, mReachedBarColor);
        mTextSize = typedArray.getDimension(R.styleable.CustomProgressBar_text_size, DEFAULT_TEXT_SIZE);
        mTextOffset = typedArray.getDimension(R.styleable.CustomProgressBar_text_offset, DEFAULT_TEXT_OFFSET);
        mTextVisibility = typedArray.getInt(R.styleable.CustomProgressBar_text_visibility, DEFAULT_TEXT_VISIBLE);

        int index = typedArray.getInt(R.styleable.CustomProgressBar_indicator_type, DEFAULT_INDICATOR_TYPE);
        mIndicatorType = IndicatorType.values()[index];

        typedArray.recycle();

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setColor(mReachedBarColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);

        int specHeight = MeasureSpec.getSize(heightMeasureSpec);
        int height;
        int paddingY = getPaddingTop() + getPaddingBottom();

        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                height = specHeight;
                break;
            case MeasureSpec.AT_MOST:
                height = (int) Math.min(mIndicatortHeight + paddingY
                        + 2 * (mTextSize + mTextOffset), specHeight);
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                height = specHeight;
                break;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int halfHeight = getHeight() / 2;
        int progressEndX = (int) (getWidth() * getProgress() / 100f) + getPaddingLeft();

        // draw the filled portion of the bar
        mProgressPaint.setStrokeWidth(mBarThickness);
        mProgressPaint.setColor(mReachedBarColor);
        canvas.drawLine(getPaddingLeft(), halfHeight, progressEndX, halfHeight, mProgressPaint);

        // draw the unfilled portion of the bar
        mProgressPaint.setColor(mUnreachedBarColor);
        canvas.drawLine(progressEndX, halfHeight, getWidth()- getPaddingRight(), halfHeight, mProgressPaint);

        // draw indicator
        if (getProgress() != 0) {
            mIndicatorPaint.setStrokeWidth(mIndicatorWidth);
            switch (mIndicatorType) {
                case Line:
                    canvas.drawLine(
                            progressEndX - (mIndicatorWidth / 2),
                            halfHeight - (mIndicatortHeight / 2),
                            progressEndX - (mIndicatorWidth / 2),
                            halfHeight + (mIndicatortHeight / 2),
                            mIndicatorPaint);
                    break;
                case Square:
                    canvas.drawRect(
                            progressEndX - mIndicatortHeight,
                            halfHeight- (mIndicatortHeight / 2),
                            progressEndX,
                            halfHeight+ mIndicatortHeight / 2,
                            mIndicatorPaint);
                    break;
                case Circle:
                    canvas.drawCircle(
                            progressEndX - mIndicatortHeight / 2,
                            halfHeight, mIndicatortHeight / 2,
                            mIndicatorPaint);
                    break;
            }
        }

        // draw text
        if (mTextVisibility == VISIBLE && getProgress() != 0) {
            String progressText = getProgress() + "%";
            float textWidth = mTextPaint.measureText(progressText);
            float textStartX = progressEndX - (textWidth / 2) - (mIndicatorWidth / 2);
            float textStartY = halfHeight - (mIndicatortHeight / 2) - mTextOffset;
            canvas.drawText(progressText, textStartX, textStartY, mTextPaint);
        }
    }

    public float getIndicatortHeight() {
        return mIndicatortHeight;
    }

    public float getIndicatorWidth() {
        return mIndicatorWidth;
    }

    public IndicatorType getIndicatorType() {
        return mIndicatorType;
    }

    public int getReachedBarColor() {
        return mReachedBarColor;
    }

    public int getUnreachedBarColor() {
        return mUnreachedBarColor;
    }

    public float getBarThickness() {
        return mBarThickness;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public float getTextOffset() {
        return mTextOffset;
    }

    public int getTextVisibility() {
        return mTextVisibility;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setIndicatortHeight(float indicatortHeight) {
        mIndicatortHeight = indicatortHeight;
        postInvalidate();
    }

    public void setIndicatorWidth(float indicatorWidth) {
        mIndicatorWidth = indicatorWidth;
        postInvalidate();
    }

    public void setIndicatorType(IndicatorType indicatorType) {
        mIndicatorType = indicatorType;
        postInvalidate();
    }

    public void setReachedBarColor(int reachedBarColor) {
        mReachedBarColor = reachedBarColor;
        postInvalidate();
    }

    public void setUnreachedBarColor(int unreachedBarColor) {
        mUnreachedBarColor = unreachedBarColor;
        postInvalidate();
    }

    public void setBarThickness(float barThickness) {
        mBarThickness = barThickness;
        postInvalidate();
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
        postInvalidate();
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        postInvalidate();
    }

    public void setTextOffset(float textOffset) {
        mTextOffset = textOffset;
        postInvalidate();
    }

    public void setTextVisibility(int visibility) {
        mTextVisibility = visibility;
        postInvalidate();
    }

    public void incrementProgressBy(int by) {
        if (by > 0) {
            setProgress(getProgress() + by);
        }

        if(mListener != null){
            mListener.onProgressChanged(getProgress());
        }
    }

    public void setProgress(int progress) {
        if (progress <= 100 && progress >= 0) {
            mProgress = progress;
            postInvalidate();
        }
    }

    public void setProgressListener(ProgressBarListener listener) {
        mListener = listener;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(SAVED_STATE, super.onSaveInstanceState());
        bundle.putInt(TEXT_COLOR, getTextColor());
        bundle.putFloat(TEXT_SIZE, getTextSize());
        bundle.putFloat(TEXT_OFFSET, getTextOffset());
        bundle.putInt(TEXT_VISIBILITY, getTextVisibility());
        bundle.putFloat(BAR_THICKNESS, getBarThickness());
        bundle.putInt(REACHED_BAR_COLOR, getReachedBarColor());
        bundle.putInt(UNREACHED_BAR_COLOR, getUnreachedBarColor());
        bundle.putFloat(INDICATOR_HEIGHT, getIndicatortHeight());
        bundle.putFloat(INDICATOR_WIDTH, getIndicatorWidth());
        bundle.putInt(CURRENT_PROGRESS, getProgress());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Parcelable) {
            final Bundle bundle = (Bundle) state;
            mTextColor = bundle.getInt(TEXT_COLOR);
            mTextSize = bundle.getFloat(TEXT_SIZE);
            mTextOffset = bundle.getFloat(TEXT_OFFSET);
            mTextVisibility = bundle.getInt(TEXT_VISIBILITY);
            mBarThickness = bundle.getFloat(BAR_THICKNESS);
            mReachedBarColor = bundle.getInt(REACHED_BAR_COLOR);
            mUnreachedBarColor = bundle.getInt(UNREACHED_BAR_COLOR);
            mIndicatortHeight = bundle.getFloat(INDICATOR_HEIGHT);
            mIndicatorWidth = bundle.getFloat(INDICATOR_WIDTH);
            setProgress(bundle.getInt(CURRENT_PROGRESS));

            state = bundle.getParcelable(SAVED_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    public float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public float sp2px(float sp) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }
}
