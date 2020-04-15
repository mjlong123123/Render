package com.dragon.render

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.dragon.render.CameraHolder.Companion.CAMERA_FRONT
import com.dragon.render.CameraHolder.Companion.CAMERA_REAR
import com.dragon.render.node.NodesRender
import com.dragon.render.node.OesTextureNode
import com.dragon.render.node.TextureNode
import com.dragon.render.texture.BitmapTexture
import com.dragon.render.texture.CombineSurfaceTexture
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {
    lateinit var customRender: CustomRender
    private val nodesRender = NodesRender(1000, 1000)
    private val cameraHolder by lazy {
        CameraHolder(glSurfaceView.context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        customRender = CustomRender(glSurfaceView, nodesRender)
        nodesRender.runInRender {
            val bitmapTexture =
                BitmapTexture(BitmapFactory.decodeStream(glSurfaceView.context.assets.open("test.jpg")))
            val textureNode = TextureNode(
                viewPortWidth.toFloat()/2,
                viewPortHeight.toFloat()/2,
                viewPortWidth.toFloat(),
                viewPortHeight.toFloat(),
                bitmapTexture
            )
            nodesRender.addNode(textureNode)
        }
        nodesRender.runInRender {
            val windowRotation = (this@MainActivity).windowManager.defaultDisplay.rotation
            val cameraRotation = cameraHolder.sensorOrientation
            val rotation = (cameraRotation - windowRotation).absoluteValue

            val surfaceTexture = CombineSurfaceTexture(640, 640, rotation, cameraHolder.cameraId == CAMERA_REAR) {
                glSurfaceView.requestRender()
            }
            val oesTextureNode = OesTextureNode(
                0f,
                0f,
                viewPortWidth.toFloat(),
                800f,
                surfaceTexture!!
            )
            nodesRender.addNode(oesTextureNode)
            cameraHolder.setSurface(surfaceTexture.surface)
                .requestPreview().invalidate()
        }
        cameraHolder.selectCamera(CAMERA_FRONT)
        switchCamera.setOnClickListener {
            val cameraId = when (cameraHolder.cameraId) {
                CAMERA_REAR -> CAMERA_FRONT
                else -> CAMERA_REAR
            }
            cameraHolder.selectCamera(cameraId).invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
        cameraHolder.requestPreview().invalidate()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
        cameraHolder.stopPreview().invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHolder.release().invalidate()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraHolder.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
