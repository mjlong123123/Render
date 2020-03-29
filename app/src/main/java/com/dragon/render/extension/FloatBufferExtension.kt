package com.dragon.render.extension

import android.graphics.Matrix
import java.nio.FloatBuffer

fun FloatBuffer.assignOpenGlPosition(x: Float, y: Float, w: Float, h: Float): FloatBuffer {
    rewind()
    put(x).put(y + h)
    put(x).put(y)
    put(x + w).put(y + h)
    put(x + w).put(y)
    rewind()
    return this
}

fun FloatBuffer.assignOpenGLTextureCoordinate(
    textureWidth: Float,
    textureHeight: Float,
    windowWidth: Float,
    windowHeight: Float,
    rotate: Float = 0f,
    flipX: Boolean = false,
    flipY: Boolean = false
) {
    var rotatedTextureWidth = textureWidth
    var rotatedTextureHeight = textureHeight
    if (rotate.toInt() % 180 != 0) {
        rotatedTextureWidth = textureHeight
        rotatedTextureHeight = textureWidth
    }
    val windowRatio = windowWidth * 1.0f / windowHeight
    val textureRatio = rotatedTextureWidth * 1.0f / rotatedTextureHeight
    var scaleX = 1.0f
    var scaleY = 1.0f

    if (textureRatio > windowRatio) {
        val scaledTextureWidth = windowHeight * textureRatio
        scaleX = windowWidth * 1.0f / scaledTextureWidth
    } else {
        val scaledTextureHeight = windowWidth / textureRatio
        scaleY = windowHeight * 1.0f / scaledTextureHeight
    }
    if (flipX) {
        scaleX *= -1
    }
    if (flipY) {
        scaleY *= -1
    }
    val vertexArray = floatArrayOf(
        0f, 1f,
        0f, 0f,
        1f, 1f,
        1f, 0f
    )
    val matrix = Matrix()
    matrix.setRotate(rotate, 0.5f, 0.5f)
    matrix.preScale(scaleX, scaleY, 0.5f, 0.5f)
    matrix.mapPoints(vertexArray)
    clear()
    put(vertexArray)
    rewind()
}