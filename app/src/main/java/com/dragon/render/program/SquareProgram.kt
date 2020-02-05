package com.dragon.render.program

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class SquareProgram : BaseProgram( """
            attribute vec2 vPosition;
            void main(){
                gl_Position = vec4(vPosition,1.0,1.0);
            }
        """,
    """
             void main(){
                gl_FragColor = vec4(1.0,0.0,0.0,1.0);
            }
        """
    ) {
    private val vPositionHandle: Int by lazy {
        GLES20.glGetAttribLocation(programHandle, "vPosition")
    }

    private val positionBuffer: FloatBuffer by lazy {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val ret = byteBuffer.asFloatBuffer()
        ret.put(0.0f).put(0.0f).position(0)
        ret
    }

    fun setSquare(left:Float,top:Float,right:Float,bottom:Float) {
        positionBuffer.position(0)
        positionBuffer.put(left).put(top).put(left).put(bottom).put(right).put(top).put(right).put(bottom).position(0)
    }

    override fun drawContent(vpMatrix:FloatArray) {
        GLES20.glEnableVertexAttribArray(vPositionHandle)
        GLES20.glVertexAttribPointer(vPositionHandle, 2, GLES20.GL_FLOAT, false, 0, positionBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(vPositionHandle)
    }
}