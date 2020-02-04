package com.dragon.render.program

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TriangleProgram : BaseProgram() {
    override val vertexShader = """
            attribute vec2 vPosition;
            void main(){
                gl_Position = vec4(vPosition,1.0,1.0);
            }
        """
    override val fragmentShader = """
             void main(){
                gl_FragColor = vec4(1.0,0.0,0.0,1.0);
            }
        """

    private val vPositionHandle: Int by lazy {
        GLES20.glGetAttribLocation(programHandle, "vPosition")
    }

    private val positionBuffer: FloatBuffer by lazy {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val ret = byteBuffer.asFloatBuffer()
        ret.put(0.0f).put(0.0f).position(0)
        ret
    }

    fun setTriangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        positionBuffer.position(0)
        positionBuffer.put(x1).put(y1).put(x2).put(y2).put(x3).put(y3).position(0)
    }

    override fun drawContent() {
        GLES20.glEnableVertexAttribArray(vPositionHandle)
        GLES20.glVertexAttribPointer(vPositionHandle, 2, GLES20.GL_FLOAT, false, 0, positionBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3)
        GLES20.glDisableVertexAttribArray(vPositionHandle)
    }
}