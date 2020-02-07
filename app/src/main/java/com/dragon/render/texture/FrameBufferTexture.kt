package com.dragon.render.texture

import android.graphics.Rect
import android.opengl.GLES20
import android.util.Log
import com.dragon.render.OpenGlMatrix

class FrameBufferTexture(width: Int, height: Int) : BasicTexture(width, height) {
    var frameBuffer: Int = 0
    private val savedRect = Rect()
    var openGlMatrix = OpenGlMatrix(width, height)

    init {
        val frameBufferArray = intArrayOf(1)
        val textureArray = intArrayOf(1)
        GLES20.glGenFramebuffers(1, frameBufferArray, 0)
        GLES20.glGenTextures(1, textureArray, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureArray[0])
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width,
            height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
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
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(
                "GroupFrameBuffer",
                "Error creating frame buffer frameBufferArray $frameBufferArray textureArray $textureArray"
            )
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferArray[0])
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            textureArray[0],
            0
        )
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        frameBuffer = frameBufferArray[0]
        textureId = textureArray[0]
    }

    fun bindFrameBuffer(block: FrameBufferTexture.() -> Unit): FrameBufferTexture {
        val intValues = IntArray(4)
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, intValues, 0)
        savedRect.set(intValues[0], intValues[1], intValues[2], intValues[3])
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer)
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        block()
        return this
    }

    fun unbindFrameBuffer(): FrameBufferTexture {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glViewport(savedRect.left, savedRect.top, savedRect.right, savedRect.bottom)
        return this
    }
}