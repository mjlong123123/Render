package com.dragon.render

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import com.dragon.render.program.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CustomRender(val glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer, View.OnTouchListener{
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

    private val textureProgram: TextureProgram by lazy {
        val resources = glSurfaceView.resources
        val context = glSurfaceView.context
        val bitmap = BitmapFactory.decodeStream(context.assets.open("global_lut.png"))
        TextureProgram(bitmap).apply { init() }
    }

    val vpMatrix = FloatArray(16)
    val projectionMatrix = FloatArray(16)
    val viewMatrix = FloatArray(16)

    var ratio = 1.0f

    var pointX : Float = 0f
    var pointY : Float = 0f

    var viewPortWidth: Int = 1
    var viewPortHeight : Int = 1

    init {
        glSurfaceView.apply {
            setEGLContextClientVersion(2)
            setRenderer(this@CustomRender)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            preserveEGLContextOnPause = true
            setOnTouchListener(this@CustomRender)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        //clear screen.
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
//        Matrix.setIdentityM(vpMatrix,0)
//        pointerProgram.setMVPMatrix(vpMatrix)
        pointerProgram.setPosition(pointX,viewPortHeight - pointY)
        pointerProgram.draw(vpMatrix)

        textureProgram.setSquare(0f,0f,viewPortWidth.toFloat(),viewPortHeight.toFloat())
        textureProgram.draw(vpMatrix)
//        lineProgram.setLine(0f,0f,0.3f,0.5f)
//        lineProgram.draw()
//
//        lineProgram.setLine(-0.5f,-0.5f,-0.3f,-0.8f)
//        lineProgram.draw()

//        triangleProgram.setTriangle(-1f,0f,1f,0f,0f,1f)
//        triangleProgram.draw(vpMatrix)
//
//        squareProgram.setSquare(-0.5f,0f,0f,-0.5f)
//        squareProgram.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged width $width height $height ")
        GLES20.glViewport(0,0,width,height)
        viewPortWidth = width
        viewPortHeight = height
        ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix,0,0f,width.toFloat(),0f,height.toFloat(),3f,15f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        // Calculate the projection and view transformation
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated ")
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let {
            when(it.actionMasked){
                MotionEvent.ACTION_DOWN ->{
                    pointX = event.x
                    pointY = event.y
                    (v as GLSurfaceView).requestRender()
                }
            }
        }
        return true
    }
}