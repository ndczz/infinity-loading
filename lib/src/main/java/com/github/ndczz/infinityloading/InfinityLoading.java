package com.github.ndczz.infinityloading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * Infinity Loading
 * Created by Alex on 22.01.2016.
 */
public class InfinityLoading extends View {
    private static final int STEPS = 200;
    private static final float PRECISION = 0.001f;

    private int backColor = 0xAA000000;
    private int progressColor = 0xAAAA0000;
    private int strokeWidth = 4;
    private int defaultRadius = 40;
    private boolean drawBack = true;
    private boolean reverse = false;

    private Paint backPaint = new Paint();
    private Paint progressPaint = new Paint();
    private Paint progressEndPaint = new Paint();

    private Path backPath = new Path();
    private Path progressPath = new Path();
    private PathMeasure backPathMeasure = new PathMeasure();

    private RectF bounds = new RectF(0, 0, 10, 10);
    private float[] progressStartCoords = new float[2];
    private float[] progressEndCoords = new float[2];
    private float[] tempTan = new float[2];
    private float normalSpeed = 1f;
    private float growSpeed = 3f;
    private float minProgressLength = 24f;
    private float backPathLength = 480f;
    private boolean isGrowing = true;
    private float progressStartOffset = 0f;
    private float progressEndOffset = minProgressLength;
    private boolean restored = false;
    private float savedBackPathLength = -1;

    public InfinityLoading(Context context) {
        super(context);
        initPaints();
        initPath();
    }

    public InfinityLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttrs(context.obtainStyledAttributes(attrs, R.styleable.InfinityLoading));
        initPaints();
        initPath();
    }

    private void processAttrs(TypedArray attrs) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        defaultRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, defaultRadius, dm);
        strokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, strokeWidth, dm);
        backColor = attrs.getColor(R.styleable.InfinityLoading_infl_backColor, backColor);
        progressColor = attrs.getColor(R.styleable.InfinityLoading_infl_progressColor, progressColor);
        strokeWidth = (int) attrs.getDimension(R.styleable.InfinityLoading_infl_strokeWidth, strokeWidth);
        drawBack = attrs.getBoolean(R.styleable.InfinityLoading_infl_drawBack, drawBack);
        reverse = attrs.getBoolean(R.styleable.InfinityLoading_infl_reverse, false);
        attrs.recycle();
    }

    private void initPaints() {
        backPaint = new Paint();
        backPaint.setAntiAlias(true);
        backPaint.setColor(backColor);
        backPaint.setStyle(Paint.Style.STROKE);
        backPaint.setStrokeWidth(strokeWidth);

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);

        progressEndPaint = new Paint(progressPaint);
        progressEndPaint.setStyle(Paint.Style.FILL);
        progressEndPaint.setStrokeWidth(strokeWidth / 2);
    }

    private void initPath() {
        backPath.reset();
        PointF c = new PointF(bounds.centerX(), bounds.centerY());
        float r = (c.x - bounds.left) / 2f - strokeWidth;
        backPath.moveTo(c.x - r, c.y + r);
        backPath.cubicTo(c.x - r - r / 2, c.y + r,
                c.x - 2 * r, c.y + r / 2,
                c.x - 2 * r, c.y);
        backPath.cubicTo(c.x - 2 * r, c.y - r / 2,
                c.x - r - r / 2, c.y - r,
                c.x - r, c.y - r);
        backPath.cubicTo(c.x - r + r / 2, c.y - r,
                c.x - r / 4, c.y - r / 2,
                c.x, c.y);
        backPath.cubicTo(c.x + r / 4, c.y + r / 2,
                c.x + r - r / 2, c.y + r,
                c.x + r, c.y + r);
        backPath.cubicTo(c.x + r + r / 2, c.y + r,
                c.x + 2 * r, c.y + r / 2,
                c.x + 2 * r, c.y);
        backPath.cubicTo(c.x + 2 * r, c.y - r / 2,
                c.x + r + r / 2, c.y - r,
                c.x + r, c.y - r);
        backPath.cubicTo(c.x + r - r / 2, c.y - r,
                c.x + r / 4, c.y - r / 2,
                c.x, c.y);
        backPath.cubicTo(c.x - r / 4, c.y + r / 2,
                c.x - r + r / 2, c.y + r,
                c.x - r, c.y + r);

        backPathMeasure.setPath(backPath, true);
        float oldBackPathLenght = backPathLength;
        if (restored) {
            restored = false;
            oldBackPathLenght = savedBackPathLength;
        }
        backPathLength = backPathMeasure.getLength();
        if (compareFloats(oldBackPathLenght, backPathLength) != 0) {
            progressEndOffset = progressEndOffset * backPathLength / oldBackPathLenght;
            progressStartOffset = progressStartOffset * backPathLength / oldBackPathLenght;
        }
        updateSpeed();
        minProgressLength = 10 * backPathLength / STEPS;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        updateProgress();

        if (drawBack) {
            canvas.drawPath(backPath, backPaint);
        }
        canvas.drawPath(progressPath, progressPaint);
        canvas.drawCircle(progressStartCoords[0], progressStartCoords[1], strokeWidth / 2, progressEndPaint);
        canvas.drawCircle(progressEndCoords[0], progressEndCoords[1], strokeWidth / 2, progressEndPaint);

        handler.sendEmptyMessageDelayed(11, 10);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            invalidate();
        }
    };  
      

    private void updateProgress() {
        progressPath.reset();
        if (reverse) {
            if (progressStartOffset < 0)
                progressStartOffset += backPathLength;
            if (progressEndOffset < 0)
                progressEndOffset += backPathLength;
        } else {
            if (progressStartOffset > backPathLength)
                progressStartOffset -= backPathLength;
            if (progressEndOffset > backPathLength)
                progressEndOffset -= backPathLength;
        }
        if (progressEndOffset > progressStartOffset) {
            backPathMeasure.getSegment(progressStartOffset, progressEndOffset, progressPath, true);
        } else {
            backPathMeasure.getSegment(progressStartOffset, backPathLength, progressPath, true);
            backPathMeasure.getSegment(0, progressEndOffset, progressPath, true);
        }
        progressPath.rLineTo(0, 0);

        backPathMeasure.getPosTan(progressStartOffset, progressStartCoords, tempTan);
        backPathMeasure.getPosTan(progressEndOffset, progressEndCoords, tempTan);

        progressStartOffset += normalSpeed;
        progressEndOffset += normalSpeed;
        if (isGrowing) {
            progressStartOffset += growSpeed;
        } else {
            progressEndOffset += growSpeed;
        }
        double progressLength = Math.abs(progressEndOffset - progressStartOffset);
        if (progressLength < minProgressLength
                || progressLength > backPathLength - minProgressLength) {
            isGrowing = !isGrowing;
        }
    }

    private void updateSpeed() {
        int direction = reverse ? -1 : 1;
        normalSpeed = backPathLength / STEPS * direction;
        growSpeed = normalSpeed;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = defaultRadius * 2 + this.getPaddingLeft() + this.getPaddingRight();
        int viewHeight = defaultRadius + this.getPaddingTop() + this.getPaddingBottom();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(viewWidth, widthSize);
        } else {
            width = viewWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(viewHeight, heightSize);
        } else {
            height = viewHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setBounds(w, h);
        initPaints();
        initPath();
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putFloat("progressStartOffset", progressStartOffset);
        bundle.putFloat("progressEndOffset", progressEndOffset);
        bundle.putBoolean("isGrowing", isGrowing);
        bundle.putBoolean("reverse", reverse);
        bundle.putFloat("backPathLength", backPathLength);
        bundle.putParcelable("super", super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            progressStartOffset = bundle.getFloat("progressStartOffset");
            progressEndOffset = bundle.getFloat("progressEndOffset");
            isGrowing = bundle.getBoolean("isGrowing");
            reverse = bundle.getBoolean("reverse");
            savedBackPathLength = bundle.getFloat("backPathLength");
            state = bundle.getParcelable("super");
        }
        restored = true;
        super.onRestoreInstanceState(state);
    }

    private void setBounds(int layoutWidth, int layoutHeight) {
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int height = Math.min((layoutWidth - paddingLeft - paddingRight) / 2,
                layoutHeight - paddingBottom - paddingTop);
        int width = 2 * height;
        int xOffset = (layoutWidth - paddingLeft - paddingRight - width) / 2 + paddingLeft;
        int yOffset = (layoutHeight - paddingTop - paddingBottom - height) / 2 + paddingTop;
        bounds = new RectF(xOffset, yOffset, xOffset + width, yOffset + height);
    }

    public int getBackColor() {
        return backColor;
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
        initPaints();
    }

    public int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        initPaints();
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        initPaints();
        initPath();
    }

    public boolean isDrawBack() {
        return drawBack;
    }

    public void setDrawBack(boolean drawBack) {
        this.drawBack = drawBack;
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
        updateSpeed();
    }

    private int compareFloats(float f1, float f2) {
        if (Math.abs(f1 - f2) < PRECISION) {
            return 0;
        } else {
            if (f1 < f2) {
                return -1;
            } else {
                return 1;
            }
        }
    }

}
