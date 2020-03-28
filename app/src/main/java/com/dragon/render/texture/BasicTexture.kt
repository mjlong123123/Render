package com.dragon.render.texture

import android.opengl.GLES20
import com.dragon.render.OpenGlUtils

open class BasicTexture(val width: Int, val height: Int) {
    var textureId: Int = 0
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
        val ids = IntArray(1)
        ids[0] = textureId
        GLES20.glDeleteTextures(1, ids, 0)
    }
}