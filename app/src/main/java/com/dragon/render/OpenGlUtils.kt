package com.dragon.render

import android.opengl.GLES20
import android.util.Log

class OpenGlUtils {
    companion object {
        private const val TAG = "OpenGlUtils"
        const val VERTEX_SHADER_CODE = """
            attribute vec2 vPosition;
            void main(){
                gl_Position = vec4(vPosition,1.0,1.0);
                gl_PointSize = 6.0 * 6.0;
            }
        """
        const val FRAGMENT_SHADER_CODE = """
            void main(){
                gl_FragColor = vec4(1.0,0.0,0.0,1.0);
            }
        """

        fun createProgram(vertexShader: String, fragmentShader: String): Int {
            val vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader)
            val fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
            if (vertex == 0 || fragment == 0) {
                Log.d(TAG, "load error vertex:$vertex")
                Log.d(TAG, "load error fragmentShader:$fragmentShader")
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

        fun destroyeProgram(program: Int) {
            GLES20.glDeleteProgram(program)
        }

        private fun loadShader(type: Int, codeString: String): Int {
            val compiled = intArrayOf(1)
            val shader = GLES20.glCreateShader(type).also {  }
            GLES20.glShaderSource(shader, codeString)
            GLES20.glCompileShader(shader)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.d(TAG, "Compilation\n" + GLES20.glGetShaderInfoLog(shader))
                return 0
            }
            return shader
        }
    }
}