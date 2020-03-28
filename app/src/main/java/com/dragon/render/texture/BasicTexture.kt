package com.dragon.render.texture

import android.opengl.GLES20
import com.dragon.render.OpenGlUtils

open class BasicTexture(val width: Int, val height: Int) {
    var released: Boolean = false
    var textureId: Int = GLES20.GL_NONE
    var targetWidth = width
    var targetHeight = height
    open val textureCoordinate = OpenGlUtils.BufferUtils.generateFloatBuffer(
        OpenGlUtils.TextureCoordinateUtils.generateTextureCoordinate(
            width,
            height,
            targetWidth,
            targetHeight
        )
    )

    open fun crop(targetWidth: Int, targetHeight: Int) {
        this.targetHeight = targetHeight
        this.targetWidth = targetWidth
        textureCoordinate.rewind()
        textureCoordinate.put(
            OpenGlUtils.TextureCoordinateUtils.generateTextureCoordinate(
                width,
                height,
                targetWidth,
                targetHeight
            )
        )
        textureCoordinate.rewind()
    }

    open fun release() {
        OpenGlUtils.releaseTexture(textureId)
        released = true
    }
}