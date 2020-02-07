package com.dragon.render

import android.opengl.GLES20
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class OpenGlUtils {
    companion object {
        private const val TAG = "OpenGlUtils"
        fun createProgram(vertexShader: String, fragmentShader: String): Int {
            val vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader)
            if (vertex == 0) {
                Log.d(TAG, "load error vertex:$vertex")
                return 0
            }
            val fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
            if (fragment == 0) {
                Log.d(TAG, "load error fragmentShader:$fragment")
                return 0
            }

            var program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertex)
            GLES20.glAttachShader(program, fragment)
            GLES20.glLinkProgram(program)
            val link = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link, 0)
            if (link[0] <= 0) {
                Log.d(TAG, "Linking Failed")
                Log.d(TAG, "Linking vertex:$vertex")
                Log.d(TAG, "Linking fragmentShader:$fragmentShader")
                GLES20.glDeleteProgram(program)
                program = 0
            }
            GLES20.glDeleteShader(vertex)
            GLES20.glDeleteShader(fragment)
            return program
        }

        private fun loadShader(type: Int, codeString: String): Int {
            val compiled = intArrayOf(1)
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, codeString)
            GLES20.glCompileShader(shader)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.d(TAG, "Compilation\n" + GLES20.glGetShaderInfoLog(shader))
                return 0
            }
            return shader
        }

        fun destroyeProgram(program: Int) {
            GLES20.glDeleteProgram(program)
        }
    }

    class BufferUtils {
        companion object {
            fun generateFloatBuffer(size: Int) =
                ByteBuffer.allocateDirect(4 * size)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer() as FloatBuffer

            fun generateFloatBuffer(arrayBuffer: FloatArray) =
                ByteBuffer.allocateDirect(4 * arrayBuffer.size)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(arrayBuffer)
                    .rewind() as FloatBuffer
        }
    }

    class TextureCoordinateUtils {
        companion object {
            fun generateTextureCoordinate(
                textureWidth: Int,
                textureHeight: Int,
                targetWidth: Int,
                targetHeight: Int
            ): FloatArray {
                val containerRatio = targetWidth.toFloat() / targetHeight
                val ratio = textureWidth.toFloat() / textureHeight
                var t = 1f
                var b = 0f
                var l = 0f
                var r = 1f
                if (containerRatio > ratio) {
                    val requestTargetHeight = textureWidth / containerRatio
                    val offset = (textureHeight - requestTargetHeight) / 2
                    t = (requestTargetHeight + offset) / textureHeight
                    b = offset / textureHeight
                } else {
                    val requestTargetWidth = textureHeight * containerRatio
                    val offset = (textureWidth - requestTargetWidth) / 2
                    l = offset / textureWidth
                    r = (requestTargetWidth + offset) / textureWidth
                }
                return floatArrayOf(
                    l, t,
                    l, b,
                    r, t,
                    r, b
                )
            }
        }
    }
}