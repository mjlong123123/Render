package com.dragon.render.texture

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils

class BitmapTexture(bitmap: Bitmap) : BasicTexture(bitmap.width, bitmap.height) {
    init {
        val textureArray = IntArray(1)
        GLES20.glGenTextures(1, textureArray, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureArray[0])
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        textureId = textureArray[0]
    }
}