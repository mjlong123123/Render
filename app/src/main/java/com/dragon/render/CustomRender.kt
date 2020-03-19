package com.dragon.render

import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import com.dragon.render.extension.assignOpenGlPosition
import com.dragon.render.program.OesTextureProgram
import com.dragon.render.program.PrimitiveProgram
import com.dragon.render.program.TextureExternalOESShaderProgramHelper
import com.dragon.render.program.TextureProgram
import com.dragon.render.texture.BitmapTexture
import com.dragon.render.texture.FrameBufferTexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CustomRender(val glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer,
    View.OnTouchListener {
    companion object {
        const val TAG = "CustomRender"
    }

    private val surfaceUnits = mutableListOf<SurfaceUnit>()

    private val oesTextureProgram:OesTextureProgram by lazy {
        OesTextureProgram()
    }
    private val oesShaderProgramHelper: TextureExternalOESShaderProgramHelper by lazy{
        TextureExternalOESShaderProgramHelper().apply {
            initProgram()
        }
    }
    private val textureProgram: TextureProgram by lazy { TextureProgram() }
    private val primitiveProgram by lazy { PrimitiveProgram() }

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
    var openGlMatrix = OpenGlMatrix(1, 1)

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
        surfaceUnits.forEach {
            it.surfaceTexture.updateTexImage()
        }
        //clear screen.
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        frameBuffer.bindFrameBuffer {
            bitmapTexture.crop(width, height / 4)
            textureProgram.draw(
                bitmapTexture.textureId,
                OpenGlUtils.BufferUtils.generateFloatBuffer(8).assignOpenGlPosition(
                    0f,
                    0f,
                    width.toFloat(),
                    height / 4.toFloat()
                ),
                bitmapTexture.textureCoordinate,
                openGlMatrix.mvpMatrix
            )
        }.unbindFrameBuffer()


        //draw framebuffer
        frameBuffer.crop(viewPortWidth / 2, viewPortHeight / 2)
        textureProgram.draw(
            frameBuffer.textureId,
            OpenGlUtils.BufferUtils.generateFloatBuffer(8).assignOpenGlPosition(
                viewPortWidth.toFloat() / 2,
                0f,
                viewPortWidth.toFloat() / 2,
                viewPortHeight.toFloat() / 2
            ),
            frameBuffer.textureCoordinate,
            openGlMatrix.mvpMatrix
        )


        //////ok
        bitmapTexture.crop(viewPortWidth, viewPortHeight)
        textureProgram.draw(
            bitmapTexture.textureId,
            OpenGlUtils.BufferUtils.generateFloatBuffer(8).assignOpenGlPosition(
                0f,
                viewPortHeight.toFloat() / 2,
                viewPortWidth.toFloat() / 2,
                viewPortHeight.toFloat() / 2
            ),
            bitmapTexture.textureCoordinate,
            openGlMatrix.mvpMatrix
        )

        primitiveProgram.draw(
            OpenGlUtils.BufferUtils.generateFloatBuffer(
                floatArrayOf(
                    pointX,
                    viewPortHeight - pointY
                )
            ),
            openGlMatrix.mvpMatrix, GLES20.GL_POINTS,
            OpenGlUtils.BufferUtils.generateFloatBuffer(floatArrayOf(0.5f, 0.5f, 0.5f)),
            6
        )

        primitiveProgram.draw(
            OpenGlUtils.BufferUtils.generateFloatBuffer(
                floatArrayOf(
                    viewPortWidth.toFloat() / 2,
                    viewPortHeight.toFloat() / 2,
                    viewPortWidth.toFloat(),
                    viewPortHeight.toFloat()
                )
            ),
            openGlMatrix.mvpMatrix, GLES20.GL_LINE_STRIP,
            OpenGlUtils.BufferUtils.generateFloatBuffer(floatArrayOf(0.5f, 0.5f, 0.5f)),
            6
        )
        primitiveProgram.draw(
            OpenGlUtils.BufferUtils.generateFloatBuffer(8).assignOpenGlPosition(
                0f,
                0f,
                viewPortWidth.toFloat() / 2,
                viewPortHeight.toFloat() / 2
            ),
            openGlMatrix.mvpMatrix, GLES20.GL_TRIANGLE_STRIP,
            OpenGlUtils.BufferUtils.generateFloatBuffer(floatArrayOf(0.5f, 0.5f, 0.5f)),
            6
        )
        surfaceUnits.firstOrNull()?.let {
                oesTextureProgram.draw(
                    it.textureId,
                    OpenGlUtils.BufferUtils.generateFloatBuffer(8).assignOpenGlPosition(
                        0f,
                        0f,
                        viewPortWidth.toFloat() / 2,
                        viewPortWidth.toFloat() / 2
                    ),
                    bitmapTexture.textureCoordinate,
                    openGlMatrix.mvpMatrix
                )
        }
        if(surfaceUnits.size > 1) {
            surfaceUnits[1].let {
                oesTextureProgram.draw(
                    it.textureId,
                    OpenGlUtils.BufferUtils.generateFloatBuffer(8).assignOpenGlPosition(
                        viewPortWidth.toFloat() / 2,
                        viewPortHeight.toFloat() / 2,
                        viewPortWidth.toFloat(),
                        viewPortHeight.toFloat()
                    ),
                    bitmapTexture.textureCoordinate,
                    openGlMatrix.mvpMatrix
                )
            }
        }
//        surfaceId?.let {
//            oesShaderProgramHelper.renderBegin()
//            oesShaderProgramHelper.render(it, GLUtils.SQUARE_COORDINATES_BUFFER,GLUtils.TEXTURE_VERTICES_BUFFER)
//            oesShaderProgramHelper.renderEnd()
//        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged width $width height $height ")
        GLES20.glViewport(0, 0, width, height)
        viewPortWidth = width
        viewPortHeight = height
        openGlMatrix = OpenGlMatrix(width, height)
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

    suspend fun generateSurface(name: String) = suspendCoroutine<SurfaceUnit> {
        glSurfaceView.queueEvent {
            val textureArrays = IntArray(1)
            GLES20.glGenTextures(1, textureArrays, 0)
            val textureId = textureArrays[0]
            val surfaceTexture = SurfaceTexture(textureId).apply {
                setOnFrameAvailableListener {
                    glSurfaceView.requestRender()
                }
            }
            val surface = Surface(surfaceTexture)
            val surfaceUnit = SurfaceUnit(name, surface, surfaceTexture, textureId)
            surfaceUnits.add(surfaceUnit)
            it.resume(surfaceUnit)
        }
    }
}

class SurfaceUnit(val name: String, val surface: Surface, val surfaceTexture: SurfaceTexture, val textureId:Int)