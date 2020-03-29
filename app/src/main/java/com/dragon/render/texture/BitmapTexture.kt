package com.dragon.render.texture

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.dragon.render.OpenGlUtils

class BitmapTexture(private val bitmap: Bitmap) : BasicTexture(bitmap.width, bitmap.height) {
    init {
        textureId = OpenGlUtils.createBitmapTexture(bitmap)
    }

    override fun release() {
        super.release()
        OpenGlUtils.releaseTexture(textureId)
        bitmap.recycle()
    }
}