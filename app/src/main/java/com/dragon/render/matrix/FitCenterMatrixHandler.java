/*
 * FitCenterMatrixHandler.java $version 2017. 02. 23.
 *
 * Copyright 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.dragon.render.matrix;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * @author dragon
 */
public class FitCenterMatrixHandler extends MatrixHandler {


    @Override
    protected void updateVertexMatrix(int screenWidth, int screenHeight, int sourceWidth, int sourceHeight) {
        if (screenWidth <= 0 || screenHeight <= 0 || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }
        vertexMatrixLock.lock();
        try {
            boolean isLandscape = sourceRotate % 180 != 0;
            RectF screenRectF = new RectF(0, 0, screenWidth, screenHeight);
            RectF sourceRectF = new RectF(0, 0, isLandscape ? sourceHeight : sourceWidth, isLandscape ? sourceWidth : sourceHeight);
            RectF displayRectF = new RectF();

            float scaleH = screenRectF.height() / sourceRectF.height();
            float scaleW = screenRectF.width() / sourceRectF.width();
            float realScale = scaleH < scaleW ? scaleH : scaleW;

            Matrix matrix = new Matrix();
            matrix.setScale(realScale, realScale);
            matrix.mapRect(displayRectF, sourceRectF);

            float scaleX = displayRectF.width() / screenRectF.width();
            float scaleY = displayRectF.height() / screenRectF.height();
            applyVertexMatrix.reset();
            applyVertexMatrix.setScale(scaleX, scaleY);
            needUpdateVertexMatrix = true;
        } finally {
            vertexMatrixLock.unlock();
        }
    }
}
