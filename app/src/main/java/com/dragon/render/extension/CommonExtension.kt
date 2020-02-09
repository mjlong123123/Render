package com.dragon.render.extension

import java.nio.FloatBuffer

fun FloatBuffer.assignOpenGlPosition(x: Float, y: Float, w: Float, h: Float): FloatBuffer {
    rewind()
    put(x).put(y + h)
    put(x).put(y)
    put(x + w).put(y + h)
    put(x + w).put(y)
    rewind()
    return this
}