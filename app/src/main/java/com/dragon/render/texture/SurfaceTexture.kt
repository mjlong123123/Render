package com.dragon.render.texture

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.view.Surface
import com.dragon.render.OpenGlUtils

class SurfaceTexture(width: Int, height: Int, notify: () -> Unit = {}) :
    BasicTexture(width, height) {
    private val surfaceTexture: SurfaceTexture
    val surface: Surface
    override val textureCoordinate = OpenGlUtils.BufferUtils.generateFloatBuffer(
        OpenGlUtils.TextureCoordinateUtils.generateBitmapTextureCoordinate(
            width,
            height,
            targetWidth,
            targetHeight
        )
    )

    init {
        val textureArray = IntArray(1)
        GLES20.glGenTextures(1, textureArray, 0)
        textureId = textureArray[0]
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setDefaultBufferSize(width, height)
        surfaceTexture.setOnFrameAvailableListener { notify.invoke() }
        surface = Surface(surfaceTexture)
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

    fun update() {
        surfaceTexture.updateTexImage()
    }

    override fun release() {
        super.release()
        surface.release()
        surfaceTexture.release()
    }
}