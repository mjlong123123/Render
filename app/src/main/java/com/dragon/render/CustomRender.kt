package com.dragon.render

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.SurfaceHolder
import com.dragon.render.program.LineProgram
import com.dragon.render.program.PointerProgram
import com.dragon.render.program.SquareProgram
import com.dragon.render.program.TriangleProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CustomRender(val glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer,
    SurfaceHolder.Callback {
    companion object {
        const val TAG = "CustomRender"
    }
    private val pointerProgram : PointerProgram by lazy {
        PointerProgram().apply { init() }
    }

    private val lineProgram: LineProgram by lazy {
        LineProgram().apply { init() }
    }

    private val triangleProgram : TriangleProgram by lazy {
        TriangleProgram().apply { init() }
    }

    private val squareProgram : SquareProgram by lazy {
        SquareProgram().apply { init() }
    }
    init {
        glSurfaceView.apply {
            setEGLContextClientVersion(2)
            setRenderer(this@CustomRender)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            preserveEGLContextOnPause = true
            holder.addCallback(this@CustomRender)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        //clear screen.
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        pointerProgram.setPosition(0.5f,0.5f)
        pointerProgram.draw()

        lineProgram.setLine(0f,0f,0.3f,0.5f)
        lineProgram.draw()

        lineProgram.setLine(-0.5f,-0.5f,-0.3f,-0.8f)
        lineProgram.draw()

        triangleProgram.setTriangle(-0.5f,0f,0f,0.5f,-1f,1f)
        triangleProgram.draw()

        squareProgram.setSquare(-0.5f,0f,0f,-0.5f)
        squareProgram.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged width $width height $height ")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated ")
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged $holder width $width height $height ")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.d(TAG, "surfaceDestroyed  $holder")
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.d(TAG, "surfaceCreated  $holder")
    }
}