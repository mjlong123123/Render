package com.dragon.render.texture

import android.graphics.SurfaceTexture
import android.view.Surface
import com.dragon.render.OpenGlUtils

class CombineSurfaceTexture(
    width: Int,
    height: Int,
    orientation: Int,
    flipX: Boolean = false,
    flipY: Boolean = false,
    notify: () -> Unit = {}
) :
    BasicTexture(width, height, orientation, flipX, flipY) {
    private val surfaceTexture: SurfaceTexture
    val surface: Surface

    init {
        textureId = OpenGlUtils.createTexture()
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setDefaultBufferSize(width, height)
        surfaceTexture.setOnFrameAvailableListener { notify.invoke() }
        surface = Surface(surfaceTexture)
    }

    fun update() {
        if (surface.isValid) {
            surfaceTexture.updateTexImage()
        }
    }

    override fun release() {
        super.release()
        surface.release()
        surfaceTexture.release()
    }
}