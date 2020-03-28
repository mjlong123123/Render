package com.dragon.render.texture

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.dragon.render.OpenGlUtils

class BitmapTexture(private val bitmap: Bitmap) : BasicTexture(bitmap.width, bitmap.height) {
    override val textureCoordinate = OpenGlUtils.BufferUtils.generateFloatBuffer(
        OpenGlUtils.TextureCoordinateUtils.generateBitmapTextureCoordinate(
            width,
            height,
            targetWidth,
            targetHeight
        )
    )

    init {
        textureId = OpenGlUtils.createBitmapTexture(bitmap)
    }

    override fun crop(targetWidth: Int, targetHeight: Int) {
        super.crop(targetWidth, targetHeight)
        textureCoordinate.rewind()
        textureCoordinate.put(
            OpenGlUtils.TextureCoordinateUtils.generateBitmapTextureCoordinate(
                width,
                height,
                targetWidth,
                targetHeight
            )
        ).rewind()
    }

    override fun release() {
        super.release()
        OpenGlUtils.releaseTexture(textureId)
        bitmap.recycle()
    }
}