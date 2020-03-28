package com.dragon.render

import android.opengl.GLSurfaceView
import android.util.Log
import android.view.SurfaceHolder
import com.dragon.render.node.NodesRender
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CustomRender(private val glSurfaceView: GLSurfaceView, private val nodesRender: NodesRender) :
    GLSurfaceView.Renderer, SurfaceHolder.Callback {
    companion object {
        const val TAG = "CustomRender"
    }

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
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated gl $gl")

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged gl $gl width $width height $height ")
        nodesRender.updateViewPort(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        nodesRender.render()
    }
}