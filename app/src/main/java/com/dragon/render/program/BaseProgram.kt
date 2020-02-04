package com.dragon.render.program

import android.opengl.GLES20
import com.dragon.render.OpenGlUtils

abstract class BaseProgram : IProgram {
    abstract val vertexShader : String
    abstract val fragmentShader :String

    var programHandle : Int = 0


    override fun init() {
        programHandle = OpenGlUtils.createProgram(vertexShader, fragmentShader)
    }
    override fun draw() {
        drawBefore()
        drawContent()
        drawAfter()
    }

    fun drawBefore() {
        GLES20.glUseProgram(programHandle)
    }

    abstract fun drawContent()

    fun drawAfter() {

    }

    override fun release() {
        GLES20.glDeleteProgram(programHandle)
    }
}