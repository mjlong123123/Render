package com.dragon.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.dragon.render.program.*
import com.dragon.render.texture.BitmapTexture
import com.dragon.render.texture.FrameBufferTexture
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CustomRender(val glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer,
    View.OnTouchListener {
    companion object {
        const val TAG = "CustomRender"
    }

    private val pointerProgram: PointerProgram by lazy {
        PointerProgram().apply { init() }
    }

    private val lineProgram: LineProgram by lazy {
        LineProgram().apply { init() }
    }

    private val triangleProgram: TriangleProgram by lazy {
        TriangleProgram().apply { init() }
    }

    private val squareProgram: SquareProgram by lazy {
        SquareProgram().apply { init() }
    }

    private val textureProgram: TextureProgram by lazy {
        val resources = glSurfaceView.resources
        val context = glSurfaceView.context
        val bitmap = BitmapFactory.decodeStream(context.assets.open("test.jpg"))
        TextureProgram()
    }

    private val frameBuffer by lazy { FrameBufferTexture(1080 / 2, 1794 / 2) }
    private val bitmapTexture by lazy {
        BitmapTexture(
            BitmapFactory.decodeStream(
                glSurfaceView.context.assets.open(
                    "test.jpg"
                )
            )
        )
    }

    val vpMatrix = FloatArray(16)
    val vpMatrix2 = FloatArray(16)
    val projectionMatrix = FloatArray(16)
    val viewMatrix = FloatArray(16)

    var ratio = 1.0f

    var pointX: Float = 0f
    var pointY: Float = 0f

    var viewPortWidth: Int = 1
    var viewPortHeight: Int = 1

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
        frameBuffer.bindFrameBuffer {
            textureProgram.prepareDraw()
            textureProgram.basicTexture = bitmapTexture
            textureProgram.setSquare(
                0f,
                0f,
                width.toFloat(),
                height / 4.toFloat()
            )
            Matrix.setIdentityM(vpMatrix2, 0)

            textureProgram.draw(openGlMatrix.mvpMatrix)

            pointerProgram.setPosition(1f, 1f)
            pointerProgram.draw(openGlMatrix.mvpMatrix)
        }.unbindFrameBuffer()


        //draw framebuffer
        textureProgram.prepareDraw()
        textureProgram.basicTexture = frameBuffer
        textureProgram.setSquare(
            viewPortWidth.toFloat() / 2,
            0f,
            viewPortWidth.toFloat() / 2,
            viewPortHeight.toFloat() / 2
        )
        textureProgram.draw(vpMatrix)


        //////ok
        textureProgram.basicTexture = bitmapTexture
        textureProgram.setSquare(
            0f,
            viewPortHeight.toFloat() / 2,
            viewPortWidth.toFloat() / 2,
            viewPortHeight.toFloat() / 2
        )
        textureProgram.draw(vpMatrix)

        pointerProgram.setPosition(0f, 0f)
        pointerProgram.draw(vpMatrix)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged width $width height $height ")
        GLES20.glViewport(0, 0, width, height)
        viewPortWidth = width
        viewPortHeight = height
        ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, 0f, width.toFloat(), 0f, height.toFloat(), 3f, 15f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        // Calculate the projection and view transformation
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated ")
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let {
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    pointX = event.x
                    pointY = event.y
                    (v as GLSurfaceView).requestRender()
                }
            }
        }
        return true
    }
}