package com.dragon.render.program

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import com.dragon.render.OpenGlUtils
import com.dragon.render.texture.BasicTexture
import com.dragon.render.texture.BitmapTexture

class TextureProgram {
    private val programHandle = OpenGlUtils.createProgram(
        """
            attribute vec2 vPosition;
            attribute vec2 vInputTextureCoordinate;
            uniform mat4 mvpMatrix;
            uniform mat4 textureMatrix;
            varying vec2 vTextureCoordinate;
            void main(){
                gl_Position = mvpMatrix * vec4(vPosition,0.0,1.0);
                vTextureCoordinate = (vec4(vInputTextureCoordinate,0.0,1.0)).xy;
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
    )
    private val vPosition by lazy { GLES20.glGetAttribLocation(programHandle, "vPosition") }
    private val vInputTextureCoordinate by lazy {
        GLES20.glGetAttribLocation(
            programHandle,
            "vInputTextureCoordinate"
        )
    }
    private val mvpMatrix by lazy { GLES20.glGetUniformLocation(programHandle, "mvpMatrix") }
    private val textureMatrix by lazy {
        GLES20.glGetUniformLocation(
            programHandle,
            "textureMatrix"
        )
    }
    private val inputTexture by lazy { GLES20.glGetUniformLocation(programHandle, "inputTexture") }

    private val positionBuffer = OpenGlUtils.BufferUtils.generateFloatBuffer(4 * 2)
    private val textureCoordinate = OpenGlUtils.BufferUtils.generateFloatBuffer(
        floatArrayOf(
            0f, 1.0f,
            0f, 0f,
            1f, 1f,
            1f, 0f
        )
    )
    private val mvpMatrixBuffer = OpenGlUtils.BufferUtils.generateFloatBuffer(16)

    var basicTexture: BasicTexture? = null

    fun setSquare(left: Float, top: Float, right: Float, bottom: Float) {
        positionBuffer.position(0)
        positionBuffer
            .put(left).put(top)
            .put(left).put(bottom)
            .put(right).put(top)
            .put(right).put(bottom)
            .position(0)
        basicTexture?.updateTargetSize((right - left).toInt(), (bottom - top).toInt())
    }

    fun prepareDraw() {
        GLES20.glUseProgram(programHandle)
    }

    fun draw(vpMatrix: FloatArray) {
        basicTexture?.let { texture ->
            //        Matrix.setIdentityM(modeMatrix, 0)
//        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modeMatrix, 0)
            GLES20.glEnableVertexAttribArray(vPosition)
            GLES20.glEnableVertexAttribArray(vInputTextureCoordinate)
            GLES20.glUniformMatrix4fv(mvpMatrix, 1, false, vpMatrix, 0)
//        GLES20.glUniformMatrix4fv(textureMatrix,1,false,bitmapTexture.textureMatrix)
            GLES20.glActiveTexture(GL_TEXTURE0)
            GLES20.glBindTexture(GL_TEXTURE_2D, texture.textureId)
            GLES20.glUniform1i(inputTexture, 0)
            GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, positionBuffer)
            GLES20.glVertexAttribPointer(
                vInputTextureCoordinate,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                texture.textureCoordinate
            )
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            GLES20.glBindTexture(GL_TEXTURE_2D, 0)
            GLES20.glDisableVertexAttribArray(vInputTextureCoordinate)
            GLES20.glDisableVertexAttribArray(vPosition)


        }
    }
}