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
                0f,
                0f,
                1920/2f,
                1080/2f,
                bitmapTexture
            )
            nodesRender.nodes.add(textureNode)
        }
        nodesRender.runInRender {
//            cameraHolder.selectCamera(CAMERA_REAR)
            val windowRotation = (this@MainActivity).windowManager.defaultDisplay.rotation
            val cameraRotation = cameraHolder.sensorOrientation
            val rotation = (cameraRotation - windowRotation).absoluteValue

            val surfaceTexture = CombineSurfaceTexture(640, 640, rotation) {
                glSurfaceView.requestRender()
            }
            val oesTextureNode = OesTextureNode(
                0f,
                0f,
                400f,
                400f,
                surfaceTexture!!
            )
            nodesRender.nodes.add(oesTextureNode)
            cameraHolder.setSurface(surfaceTexture.surface)
                .requestPreview().invalidate()
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
