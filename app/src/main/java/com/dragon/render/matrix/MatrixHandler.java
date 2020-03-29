/*
 * MatrixHandler.java $version 2017. 02. 23.
 *
 * Copyright 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.dragon.render.matrix;

import android.graphics.Matrix;

import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author dragon
 */
public abstract class MatrixHandler {

    protected int sourceRotate = 360;
    protected int sourceWidth = 0;
    protected int sourceHeight = 0;
    protected boolean sourceMirrorX = false;
    protected boolean sourceMirrorY = false;

    protected int screenWidth = 0;
    protected int screenHeight = 0;

    protected Matrix applyTextureMatrix;
    protected Matrix applyVertexMatrix;

    protected final ReentrantLock textureMatrixLock = new ReentrantLock(true);
    protected final ReentrantLock vertexMatrixLock = new ReentrantLock(true);

    protected boolean needUpdateTextureMatrix = false;
    protected boolean needUpdateVertexMatrix = false;

    public MatrixHandler() {
        applyTextureMatrix = new Matrix();
        applyVertexMatrix = new Matrix();
    }

    public void refreshMatrix() {
        needUpdateTextureMatrix = true;
        needUpdateVertexMatrix = true;
    }

    private static final void applyMatrix(float[] array, FloatBuffer buffer, Matrix matrix) {
        float[] dst = new float[array.length];
        matrix.mapPoints(dst, array);
        buffer.clear();
        buffer.put(dst);
        buffer.position(0);
    }

    private final void updateDisplaySizeAndSourceSize(int screenWidth, int screenHeight, int sourceWidth, int sourceHeight) {
        if ((this.sourceWidth != sourceWidth || this.sourceHeight != sourceHeight || this.screenWidth != screenWidth || this.screenHeight != screenHeight)) {

            if ((screenWidth <= 0 || screenHeight <= 0) && (sourceWidth <= 0 || sourceHeight <= 0)) {
                return;
            }
            this.sourceWidth = sourceWidth;
            this.sourceHeight = sourceHeight;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;

            updateVertexMatrix(screenWidth, screenHeight, sourceWidth, sourceHeight);
        }
    }

    protected abstract void updateVertexMatrix(int screenWidth, int screenHeight, int sourceWidth, int sourceHeight);

    public final void setSourceRotate(int rotate) {
        if (rotate != sourceRotate) {
            textureMatrixLock.lock();
            try {
                sourceRotate = rotate;
                applyTextureMatrix.reset();
                applyTextureMatrix.setRotate(360 - sourceRotate, 0.5f, 0.5f);
                applyTextureMatrix.preScale(sourceMirrorX ? -1.0f : 1.0f, sourceMirrorY ? -1.0f : 1.0f, 0.5f, 0.5f);
                needUpdateTextureMatrix = true;
            } finally {
                textureMatrixLock.unlock();
            }
        }
    }

    public final void setSourceMirror(boolean mirrorX, boolean mirrorY) {
        if (sourceMirrorX != mirrorX || sourceMirrorY != mirrorY) {
            textureMatrixLock.lock();
            try {
                sourceMirrorX = mirrorX;
                sourceMirrorY = mirrorY;
                applyTextureMatrix.reset();
                applyTextureMatrix.setRotate(360 - sourceRotate, 0.5f, 0.5f);
                applyTextureMatrix.preScale(sourceMirrorX ? -1.0f : 1.0f, sourceMirrorY ? -1.0f : 1.0f, 0.5f, 0.5f);
                needUpdateTextureMatrix = true;
            } finally {
                textureMatrixLock.unlock();
            }
        }
    }

    public final void setSourceSize(int width, int height) {
        updateDisplaySizeAndSourceSize(screenWidth, screenHeight, width, height);
    }

    public final void setDisplaySize(int screenWidth, int screenHeight) {
        updateDisplaySizeAndSourceSize(screenWidth, screenHeight, sourceWidth, sourceHeight);
    }

    public final void applyTextureMatrix(float[] array, FloatBuffer buffer) {
        if (array == null || buffer == null) {
            return;
        }
        textureMatrixLock.lock();
        try {
            if (needUpdateTextureMatrix) {
                applyMatrix(array, buffer, applyTextureMatrix);
                needUpdateTextureMatrix = false;
            }
        } finally {
            textureMatrixLock.unlock();
        }
    }

    public final void applyVertexMatrix(float[] array, FloatBuffer buffer) {
        if (array == null || buffer == null) {
            return;
        }

        vertexMatrixLock.lock();
        try {
            if (needUpdateVertexMatrix) {
                applyMatrix(array, buffer, applyVertexMatrix);
                needUpdateVertexMatrix = false;
            }
        } finally {
            vertexMatrixLock.unlock();
        }
    }


    public float getSourceHeight() {
        return sourceHeight;
    }

    public float getSourceWidth() {
        return sourceWidth;
    }
}
