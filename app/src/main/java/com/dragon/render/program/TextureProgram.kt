package com.dragon.render.program

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLUtils
import android.opengl.Matrix
import com.dragon.render.texture.BitmapTexture
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TextureProgram(bitmap: Bitmap) : BaseProgram(
    """
            attribute vec2 vPosition;
            attribute vec2 vInputTextureCoordinate;
            uniform mat4 mvpMatrix;
            varying vec2 vTextureCoordinate;
            void main(){
                gl_Position = mvpMatrix * vec4(vPosition,0.0,1.0);
                vTextureCoordinate = vInputTextureCoordinate;
            }
        """,
    """
            precision mediump float;
            uniform sampler2D inputTexture;
            varying vec2 vTextureCoordinate;
             void main(){
                gl_FragColor = texture2D(inputTexture, vTextureCoordinate);
            }
        """
) {

    private val vPositionHandle: Int by lazy {
        val index = GLES20.glGetAttribLocation(programHandle, "vPosition")
        index
    }

    private val mvpMatrixHandle: Int by lazy {
        GLES20.glGetUniformLocation(programHandle, "mvpMatrix")
    }

    private val vInputTextureCoordinateHandle : Int by lazy {
        GLES20.glGetAttribLocation(programHandle, "vInputTextureCoordinate")
    }

    private val inputTextureHandle: Int by lazy {
        GLES20.glGetUniformLocation(programHandle, "inputTexture")
    }

    private val positionBuffer: FloatBuffer by lazy {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
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

    private val textureCoordinateBuffer: FloatBuffer by lazy {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val ret = byteBuffer.asFloatBuffer()
        ret.put(0.0f).put(1.0f)
            .put(0.0f).put(0f)
            .put(1.0f).put(1.0f)
            .put(1.0f).put(0f)
            .position(0)
        ret
    }
    private val modeMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private val bitmapTexture = BitmapTexture(bitmap)

    fun setSquare(left:Float,top:Float,right:Float,bottom:Float) {
        positionBuffer.position(0)
        positionBuffer
            .put(left).put(top)
            .put(left).put(bottom)
            .put(right).put(top)
            .put(right).put(bottom)
            .position(0)
    }

    override fun drawContent(vpMatrix: FloatArray) {
        Matrix.setIdentityM(modeMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modeMatrix, 0)
        GLES20.glEnableVertexAttribArray(vPositionHandle)
        GLES20.glEnableVertexAttribArray(vInputTextureCoordinateHandle)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glBindTexture(GL_TEXTURE_2D, bitmapTexture.textureId)
        GLES20.glUniform1i(inputTextureHandle,0)
        GLES20.glVertexAttribPointer(vPositionHandle, 2, GLES20.GL_FLOAT, false, 0, positionBuffer)
        GLES20.glVertexAttribPointer(vInputTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureCoordinateBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GL_TEXTURE_2D, 0)
        GLES20.glDisableVertexAttribArray(vInputTextureCoordinateHandle)
        GLES20.glDisableVertexAttribArray(vPositionHandle)
    }
}