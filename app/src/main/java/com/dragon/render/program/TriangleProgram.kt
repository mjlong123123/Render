package com.dragon.render.program

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TriangleProgram : BaseProgram(
    """
            attribute vec2 vPosition;
            uniform mat4 mvpMatrix;
            void main(){
                gl_Position = mvpMatrix * vec4(vPosition,0.0,1.0);
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

    private val mvpMatrixHandle: Int by lazy {
        GLES20.glGetUniformLocation(programHandle, "mvpMatrix")
    }

    private val positionBuffer: FloatBuffer by lazy {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val ret = byteBuffer.asFloatBuffer()
        ret.put(0.0f).put(0.0f).position(0)
        ret
    }

    private val mvpMatrixBuffer: FloatBuffer by lazy {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 16)
        byteBuffer.order(ByteOrder.nativeOrder())
        val ret = byteBuffer.asFloatBuffer()
        ret.put(0.0f).put(0.0f).position(0)
        ret
    }

    private val modeMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    fun setTriangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        positionBuffer.position(0)
        positionBuffer.put(x1).put(y1).put(x2).put(y2).put(x3).put(y3).position(0)
    }

    fun setMVPMatrix(matrix: FloatArray) {
        mvpMatrixBuffer.position(0)
        mvpMatrixBuffer.put(matrix).position(0)
    }

    override fun drawContent(vpMatrix: FloatArray) {
        Matrix.setIdentityM(modeMatrix, 0)
//        Matrix.translateM(modeMatrix,0,0f,0.5f,0f)
//        Matrix.rotateM(modeMatrix,0,180f,0f,0f,1f)
//        Matrix.translateM(modeMatrix,0,0f,-0.5f,0f)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modeMatrix, 0)
        GLES20.glEnableVertexAttribArray(vPositionHandle)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glVertexAttribPointer(vPositionHandle, 2, GLES20.GL_FLOAT, false, 0, positionBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3)
        GLES20.glDisableVertexAttribArray(vPositionHandle)
    }
}