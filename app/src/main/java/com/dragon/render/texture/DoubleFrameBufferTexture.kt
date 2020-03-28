package com.dragon.render.texture

class DoubleFrameBufferTexture(width: Int, height: Int) {
    private val frameBufferTexture1 = FrameBufferTexture(width, height)
    private val frameBufferTexture2 = FrameBufferTexture(width, height)
    private var frontBufferTexture: FrameBufferTexture = frameBufferTexture1

    fun swap(): FrameBufferTexture {
        return if (frontBufferTexture == frameBufferTexture1) {
            frontBufferTexture = frameBufferTexture2
            frontBufferTexture.bind()
            frameBufferTexture1
        } else {
            frontBufferTexture = frameBufferTexture1
            frontBufferTexture.bind()
            frameBufferTexture2
        }
    }

    fun end(): FrameBufferTexture {
        frontBufferTexture.unbind()
        return frontBufferTexture
    }

    fun release() {
        frameBufferTexture1.release()
        frameBufferTexture2.release()
    }
}