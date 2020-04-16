package com.dragon.render.texture

import android.opengl.GLES20
import com.dragon.render.utils.OpenGlUtils

open class BasicTexture(val width: Int, val height: Int, val orientation: Int = 0, val flipX:Boolean = false, val flipY:Boolean = false) {
    var textureId: Int = GLES20.GL_NONE
    var released: Boolean = false
    open fun release() {
        OpenGlUtils.releaseTexture(textureId)
        released = true
    }
}