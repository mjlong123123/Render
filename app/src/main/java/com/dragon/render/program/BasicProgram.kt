package com.dragon.render.program

import com.dragon.render.OpenGlUtils
import java.nio.FloatBuffer

abstract class BasicProgram(
    val programKey: ProgramKey,
    vertexShader: String,
    fragmentShader: String
) {
    val programHandle = OpenGlUtils.createProgram(
        vertexShader,
        fragmentShader
    )
    private var released: Boolean = false
    abstract fun draw(
        textureId: Int,
        position: FloatBuffer,
        textureCoordinate: FloatBuffer,
        mvp: FloatArray
    )

    fun release() {
        OpenGlUtils.destroyProgram(programHandle)
        released = true
    }

    fun isReleased() = released
}