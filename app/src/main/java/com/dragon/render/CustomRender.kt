package com.dragon.render

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.SurfaceHolder
import com.dragon.render.extension.assignOpenGlPosition
import com.dragon.render.node.NodesRender
import com.dragon.render.program.ProgramKey
import com.dragon.render.program.TextureProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CustomRender(private val glSurfaceView: GLSurfaceView,private val nodesRender: NodesRender) :
    GLSurfaceView.Renderer, SurfaceHolder.Callback {
    companion object {
        const val TAG = "CustomRender"
    }
    var displayProgram: TextureProgram? = null
    var openGlMatrix = OpenGlMatrix(1, 1)

    init {
        glSurfaceView.apply {
            holder.addCallback(this@CustomRender)
            setEGLContextClientVersion(2)
            setRenderer(this@CustomRender)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            preserveEGLContextOnPause = true
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.d(TAG, "surfaceCreated  ${holder?.surface}")
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged ${holder?.surface} width $width height $height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.d(TAG, "surfaceDestroyed ${holder?.surface}")
        nodesRender.release()
        displayProgram?.release()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated gl $gl")

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged gl $gl width $width height $height ")
        this.width = width
        this.height = height
        openGlMatrix = OpenGlMatrix(width, height)
    }

    var width:Int = 0
    var height:Int = 0

    override fun onDrawFrame(gl: GL10?) {
        val frontBuffer = nodesRender.render()
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (displayProgram == null) {
            displayProgram = TextureProgram()
        }
        frontBuffer?:return
        displayProgram!!.draw(
            frontBuffer.textureId,
            OpenGlUtils.BufferUtils.generateFloatBuffer(8).assignOpenGlPosition(
                0f, 0f, width.toFloat(), height.toFloat()
            ),
            frontBuffer.textureCoordinate,
            openGlMatrix.mvpMatrix
        )
    }
}