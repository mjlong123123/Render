package com.dragon.render.texture

import com.dragon.render.OpenGlUtils

open class BasicTexture(val width: Int, val height: Int) {
    var textureId: Int = 0
    private var targetWidth = width
    private var targetHeight = height
    val textureCoordinate = OpenGlUtils.BufferUtils.generateFloatBuffer(OpenGlUtils.TextureCoordinateUtils.generateTextureCoordinate(width,height,targetWidth,targetHeight))
    fun updateTargetSize(targetWidth: Int, targetHeight: Int) {
        this.targetHeight = targetHeight
        this.targetWidth = targetWidth
        textureCoordinate.rewind()
        textureCoordinate.put(OpenGlUtils.TextureCoordinateUtils.generateTextureCoordinate(width,height,targetWidth,targetHeight))
        textureCoordinate.rewind()
    }
}