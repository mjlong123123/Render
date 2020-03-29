/*
 * CustomRectMatrixHandler.java $version 2017. 02. 23.
 *
 * Copyright 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.dragon.render.matrix;

import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * @author dragon
 */
public class CustomRectMatrixHandler extends MatrixHandler implements ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {
    private static final String TAG = "CustomRectMatrixHandler";
    private boolean initRectFlag = false;
    private RectF screenRectF;
    private RectF sourceRectF;
    private RectF displayRectF;
    private RectF minimumScaleSourceRectF;
    private RectF maximumScaleSourceRectF;

    private RectF currentScaleRectF;
    private float currentScale;

    private float minimumRealScale;
    private float maximumRealScale;

    private Matrix scaleAndTranslateMatrix = new Matrix();
    private RectF scaleAndTranslateRectF = new RectF();

    private IComponentToucheListener iComponentToucherListener;

    @Override
    protected void updateVertexMatrix(int screenWidth, int screenHeight, int sourceWidth, int sourceHeight) {
        if (screenWidth <= 0 || screenHeight <= 0 || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }

        boolean isLandscape = sourceRotate % 180 != 0;
        screenRectF = new RectF(0, 0, screenWidth, screenHeight);
        sourceRectF = new RectF(0, 0, isLandscape ? sourceHeight : sourceWidth, isLandscape ? sourceWidth : sourceHeight);
        displayRectF = new RectF(screenWidth / 4.0f, screenHeight / 4.0f, screenWidth * 3.0f / 4.0f, screenHeight * 3.0f / 4.0f);
        minimumScaleSourceRectF = new RectF();
        maximumScaleSourceRectF = new RectF();

        float scaleH = displayRectF.height() / sourceRectF.height();
        float scaleW = displayRectF.width() / sourceRectF.width();
        minimumRealScale = scaleH < scaleW ? scaleH : scaleW;
        maximumRealScale = scaleH > scaleW ? scaleH : scaleW;

        Matrix matrix = new Matrix();
        matrix.setTranslate(displayRectF.centerX() - sourceRectF.centerX(), displayRectF.centerY() - sourceRectF.centerY());
        matrix.postScale(minimumRealScale, minimumRealScale, displayRectF.centerX(), displayRectF.centerY());
        matrix.mapRect(minimumScaleSourceRectF, sourceRectF);

        matrix.reset();
        matrix.setTranslate(displayRectF.centerX() - sourceRectF.centerX(), displayRectF.centerY() - sourceRectF.centerY());
        matrix.postScale(maximumRealScale, maximumRealScale, displayRectF.centerX(), displayRectF.centerY());
        matrix.mapRect(maximumScaleSourceRectF, sourceRectF);

        currentScaleRectF = new RectF(minimumScaleSourceRectF);
        currentScale = minimumRealScale;

        initRectFlag = true;
        updateMatrix();
    }

    private void updateMatrix() {
        vertexMatrixLock.lock();
        try {
            float scaleX = currentScaleRectF.width() / screenRectF.width();
            float scaleY = currentScaleRectF.height() / screenRectF.height();

            applyVertexMatrix.reset();
            applyVertexMatrix.setScale(scaleX, scaleY);
            applyVertexMatrix.postTranslate((currentScaleRectF.centerX() - screenRectF.centerX()) * 2 / screenRectF.width(), -(currentScaleRectF.centerY() - screenRectF.centerY()) * 2 / screenRectF.height());
            needUpdateVertexMatrix = true;
        } finally {
            vertexMatrixLock.unlock();
        }
    }

    public void startFit(final IUpdateView iUpdateView) {

        float scale = minimumScaleSourceRectF.width() / currentScaleRectF.width();
        float dx = minimumScaleSourceRectF.centerX() - currentScaleRectF.centerX();
        float dy = minimumScaleSourceRectF.centerY() - currentScaleRectF.centerY();

        ValueAnimator ScaleValueAnimator = ValueAnimator.ofFloat(1.0f, scale);
        ScaleValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                scale(scale);
                if (iUpdateView != null) {
                    iUpdateView.update();
                }
            }
        });
        ScaleValueAnimator.setDuration(1000);
        ScaleValueAnimator.start();
    }

    public void startFill(final IUpdateView iUpdateView) {

        float scale = maximumScaleSourceRectF.width() / currentScaleRectF.width();
        float dx = maximumScaleSourceRectF.centerX() - currentScaleRectF.centerX();
        float dy = maximumScaleSourceRectF.centerY() - currentScaleRectF.centerY();

        ValueAnimator ScaleValueAnimator = ValueAnimator.ofFloat(1.0f, scale);
        ScaleValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                scale(scale);
                if (iUpdateView != null) {
                    iUpdateView.update();
                }
            }
        });
        ScaleValueAnimator.setDuration(1000);
        ScaleValueAnimator.start();
    }


    public void scale(float scaleFactor) {
        if (!initRectFlag) {
            return;
        }

        scaleAndTranslateMatrix.reset();
        scaleAndTranslateMatrix.setScale(scaleFactor, scaleFactor, currentScaleRectF.centerX(), currentScaleRectF.centerY());
        scaleAndTranslateMatrix.mapRect(scaleAndTranslateRectF, currentScaleRectF);
        currentScaleRectF.set(scaleAndTranslateRectF);

//		checkScaleRange();
//		checkPosition();
        updateMatrix();
    }

    public void move(float dx, float dy) {
        if (!initRectFlag) {
            return;
        }

        scaleAndTranslateMatrix.reset();
        scaleAndTranslateMatrix.setTranslate(dx, dy);
        scaleAndTranslateMatrix.mapRect(scaleAndTranslateRectF, currentScaleRectF);
        currentScaleRectF.set(scaleAndTranslateRectF);
//		checkPosition();
        updateMatrix();
    }

    private void checkScaleRange() {
        float scaleFactor;
        float scaleWidth = displayRectF.width() / scaleAndTranslateRectF.width();
        float scaleHeight = displayRectF.height() / scaleAndTranslateRectF.height();

        if (scaleWidth > 1.0f && scaleHeight > 1.0f) {//too small.
            if (scaleWidth < scaleHeight) {
                scaleFactor = scaleWidth;
            } else {
                scaleFactor = scaleHeight;
            }
            scaleAndTranslateMatrix.reset();
            scaleAndTranslateMatrix.setScale(scaleFactor, scaleFactor, currentScaleRectF.centerX(), currentScaleRectF.centerY());
            scaleAndTranslateMatrix.mapRect(scaleAndTranslateRectF, currentScaleRectF);
            currentScaleRectF.set(scaleAndTranslateRectF);
        } else if (scaleWidth < 1.0f && scaleHeight < 1.0f) {//too big.
            if (scaleWidth < scaleHeight) {
                scaleFactor = scaleHeight;
            } else {
                scaleFactor = scaleWidth;
            }
            scaleAndTranslateMatrix.reset();
            scaleAndTranslateMatrix.setScale(scaleFactor, scaleFactor, currentScaleRectF.centerX(), currentScaleRectF.centerY());
            scaleAndTranslateMatrix.mapRect(scaleAndTranslateRectF, currentScaleRectF);
            currentScaleRectF.set(scaleAndTranslateRectF);
        }
    }

    private void checkPosition() {
        float dx = 0.0f;
        float dy = 0.0f;
        if (displayRectF.width() / displayRectF.height() > scaleAndTranslateRectF.width() / scaleAndTranslateRectF.height()) {
            if (scaleAndTranslateRectF.left < displayRectF.left) {
                dx = displayRectF.left - scaleAndTranslateRectF.left;
            } else if (scaleAndTranslateRectF.right > displayRectF.right) {
                dx = displayRectF.right - scaleAndTranslateRectF.right;
            }

            if (scaleAndTranslateRectF.top > displayRectF.top) {
                dy = displayRectF.top - scaleAndTranslateRectF.top;
            } else if (scaleAndTranslateRectF.bottom < displayRectF.bottom) {
                dy = displayRectF.bottom - scaleAndTranslateRectF.bottom;
            }
        } else {
            if (scaleAndTranslateRectF.left > displayRectF.left) {
                dx = displayRectF.left - scaleAndTranslateRectF.left;
            } else if (scaleAndTranslateRectF.right < displayRectF.right) {
                dx = displayRectF.right - scaleAndTranslateRectF.right;
            }

            if (scaleAndTranslateRectF.top < displayRectF.top) {
                dy = displayRectF.top - scaleAndTranslateRectF.top;
            } else if (scaleAndTranslateRectF.bottom > displayRectF.bottom) {
                dy = displayRectF.bottom - scaleAndTranslateRectF.bottom;
            }
        }

        if (dx != 0.0f || dy != 0.0f) {
            scaleAndTranslateMatrix.reset();
            scaleAndTranslateMatrix.setTranslate(dx, dy);
            scaleAndTranslateMatrix.mapRect(scaleAndTranslateRectF, currentScaleRectF);
            currentScaleRectF.set(scaleAndTranslateRectF);
        }
    }

    public void setiComponentToucherListener(IComponentToucheListener listener) {
        iComponentToucherListener = listener;
    }

    public boolean currentRectContain(float x, float y) {
        if (!initRectFlag) {
            return false;
        }
        return currentScaleRectF.contains(x, y);
    }

    public float offsetX(float x) {
        if (!initRectFlag) {
            return x;
        }
        return x - (currentScaleRectF.left - screenRectF.left);
    }

    public float offsetY(float y) {
        if (!initRectFlag) {
            return y;
        }
        return y - (currentScaleRectF.top - screenRectF.top);
    }

    public float getCurrentScale() {
        if (!initRectFlag) {
            return 1.0f;
        }
        return sourceRectF.width() / currentScaleRectF.width();
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.i(TAG, "onScale getScaleFactor " + detector.getScaleFactor());

        scale(detector.getScaleFactor());
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.i(TAG, "onScale onScaleBegin " + detector.getScaleFactor());
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Log.i(TAG, "onScale onScaleEnd " + detector.getScaleFactor());
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.i(TAG, "onDown");
        if (iComponentToucherListener != null) {
            float x = e.getX();
            float y = e.getY();

            iComponentToucherListener.onDown(e);
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.i(TAG, "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.i(TAG, "onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.i(TAG, "onScroll distanceX " + distanceX + "  distanceY " + distanceY);
        move(-distanceX, -distanceY);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i(TAG, "onLongPress");

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.i(TAG, "onFling");
        return false;
    }

    public interface IUpdateView {
        void update();
    }

    public interface IComponentToucheListener {
        boolean onDown(MotionEvent e);
    }
}
