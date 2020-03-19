package com.dragon.render

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {
    lateinit var customRender : CustomRender

    private val cameraController by lazy {
        CameraController(glSurfaceView.context)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        customRender = CustomRender(glSurfaceView)
        GlobalScope.launch {
            val ret1 = customRender.generateSurface("camera preview")
            val ret2 = customRender.generateSurface("camera preview2")
            val ret3 = getSurfaceView()
            val ret4 = getTextureView()
            cameraController.setSurface(ret1.surface, ret2.surface,ret3,ret4)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraController.release()
    }

    suspend fun getTextureView() = suspendCoroutine<Surface> {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                surface?.let {
                    s -> it.resume(Surface(s))
                }
            }
        }
    }

    suspend fun getSurfaceView()= suspendCoroutine<Surface> {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                holder?.let {
                    h ->
                    it.resume(h.surface)
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraController.onRequestPermissionsResult(requestCode,permissions, grantResults)
    }
}
