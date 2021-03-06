package com.dragon.render.utils

import android.opengl.Matrix

class OpenGlMatrix(private val viewPortWidth: Int, private val viewPortHeight: Int) {
    companion object {
        const val MVP_INDEX = 0
        const val VP_INDEX = 16
        const val M_INDEX = 32
    }

    private val values = FloatArray(16 * 3)
    val mvpMatrix = values

    init {
        val projection = FloatArray(16)
        val viewer = FloatArray(16)
        Matrix.frustumM(
            projection,
            0,
            0f,
            viewPortWidth.toFloat(),
            0f,
            viewPortHeight.toFloat(),
            3f,
            15f
        )
        Matrix.setLookAtM(viewer, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(values,
            VP_INDEX, projection, 0, viewer, 0)
        Matrix.setIdentityM(values,
            M_INDEX
        )
        Matrix.multiplyMM(values,
            MVP_INDEX, values,
            VP_INDEX, values,
            M_INDEX
        )
    }

    fun scale(scaleX: Float, scaleY: Float): OpenGlMatrix {
        Matrix.setIdentityM(values,
            M_INDEX
        )
        Matrix.scaleM(values,
            M_INDEX, scaleX, scaleY, 1f)
        Matrix.multiplyMM(values,
            MVP_INDEX, values,
            VP_INDEX, values,
            M_INDEX
        )
        return this
    }

    fun rotate(degree: Float) {
        Matrix.setIdentityM(values,
            M_INDEX
        )
        Matrix.translateM(values,
            M_INDEX,viewPortWidth/2f,viewPortHeight/2f,0f)
        Matrix.rotateM(values,
            M_INDEX, degree, 0f, 0f, 1f)
        Matrix.translateM(values,
            M_INDEX,-viewPortWidth/2f,-viewPortHeight/2f,0f)
        Matrix.multiplyMM(values,
            MVP_INDEX, values,
            VP_INDEX, values,
            M_INDEX
        )
    }
}