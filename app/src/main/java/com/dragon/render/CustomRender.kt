package com.dragon.render

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.dragon.render.matrix.CropRectMatrixHandler
import com.dragon.render.node.NodesRender
import com.dragon.render.program.TextureProgram
import com.dragon.render.utils.OpenGlMatrix
import com.dragon.render.utils.OpenGlUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CustomRender(private val glSurfaceView: GLSurfaceView,private val nodesRender: NodesRender) :
    GLSurfaceView.Renderer, SurfaceHolder.Callback, View.OnTouchListener {
    companion object {
        const val TAG = "CustomRender"
    }
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var gestureDetectorCompat: GestureDetectorCompat
    val matrixHandler = CropRectMatrixHandler()
    var displayProgram: TextureProgram? = null
    val displayPosition = OpenGlUtils.BufferUtils.generateFloatBuffer(8)
    val displayTextureCoordinate = OpenGlUtils.BufferUtils.generateFloatBuffer(8)
    var openGlMatrix = OpenGlMatrix(1, 1)
    val matrixValues = FloatArray(16)


    init {
        glSurfaceView.apply {
            holder.addCallback(this@CustomRender)
            setEGLContextClientVersion(2)
            setRenderer(this@CustomRender)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            preserveEGLContextOnPause = true
            scaleGestureDetector = ScaleGestureDetector(glSurfaceView.context,matrixHandler)
            gestureDetectorCompat = GestureDetectorCompat(glSurfaceView.context,matrixHandler)
            glSurfaceView.setOnTouchListener(this@CustomRender)
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
//        nodesRender.release()
//        displayProgram?.release()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated gl $gl")

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged gl $gl width $width height $height ")
        this.width = width
        this.height = height
        openGlMatrix = OpenGlMatrix(width, height)
        matrixHandler.setDisplaySize(width,height)
        matrixHandler.setSourceSize(nodesRender.viewPortWidth,nodesRender.viewPortHeight)
        matrixHandler.applyVertexMatrix(OpenGlUtils.BufferUtils.SQUARE_VERTICES, displayPosition)
//        matrixHandler.applyTextureMatrix(OpenGlUtils.BufferUtils.TEXTURE_VERTICES, displayTextureCoordinate)
        displayTextureCoordinate.clear()
        displayTextureCoordinate.put(OpenGlUtils.BufferUtils.TEXTURE_VERTICES)
        displayTextureCoordinate.rewind()
        for(i in 0 until 8){
            Log.d("dragon_Point"," vertices ${displayPosition[i]}")
        }
        for(i in 0 until 8){
            Log.d("dragon_Point"," texture ${displayTextureCoordinate[i]}")
        }
        Matrix.setIdentityM(matrixValues,0)
    }

    var width:Int = 0
    var height:Int = 0

    override fun onDrawFrame(gl: GL10?) {
        val frontBuffer = nodesRender.render()
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (displayProgram == null) {
            displayProgram = TextureProgram()
        }
        frontBuffer?:return
        matrixHandler.applyVertexMatrix(OpenGlUtils.BufferUtils.SQUARE_VERTICES, displayPosition)
        displayProgram!!.draw(
            frontBuffer.textureId,
            displayPosition,
            displayTextureCoordinate,
            matrixValues
        )
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.d("dragon_Point"," onTouch")
        scaleGestureDetector.onTouchEvent(event)
        gestureDetectorCompat.onTouchEvent(event)
        glSurfaceView.requestRender()
        return true
    }
}