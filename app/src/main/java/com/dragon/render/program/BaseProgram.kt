package com.dragon.render.program

import android.opengl.GLES20
import com.dragon.render.OpenGlUtils

abstract class BaseProgram(val vertexShader : String, val fragmentShader :String) : IProgram {

    var programHandle : Int = 0


    override fun init() {
        programHandle = OpenGlUtils.createProgram(vertexShader, fragmentShader)
    }
    override fun draw(vpMatrix: FloatArray) {
        drawBefore()
        drawContent(vpMatrix)
        drawAfter()
    }

    fun drawBefore() {
        GLES20.glUseProgram(programHandle)
    }

    abstract fun drawContent(vpMatrix: FloatArray)

    fun drawAfter() {

    }

    override fun release() {
        GLES20.glDeleteProgram(programHandle)
    }
}