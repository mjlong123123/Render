package com.dragon.render.program

interface IProgram {

    fun init()
    fun draw(vpMatrix: FloatArray)
    fun release()
}