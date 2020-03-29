package com.dragon.render.texture

import android.opengl.GLES20
import com.dragon.render.OpenGlUtils

open class BasicTexture(val width: Int, val height: Int, val orientation: Int = 0) {
    var textureId: Int = GLES20.GL_NONE
    var released: Boolean = false
    open fun release() {
        OpenGlUtils.releaseTexture(textureId)
        released = true
    }
}