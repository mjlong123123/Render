package com.dragon.render

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.dragon.render.CameraHolder.Companion.CAMERA_FRONT
import com.dragon.render.node.NodesRender
import com.dragon.render.node.OesTextureNode
import com.dragon.render.node.TextureNode
import com.dragon.render.texture.BitmapTexture
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var customRender: CustomRender
    private val nodesRender = NodesRender()
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
                viewPortHeight.toFloat(),
                viewPortWidth.toFloat() / 2,
                viewPortHeight.toFloat() *3/ 4,
                bitmapTexture
            )
            nodesRender.nodes.add(textureNode)
        }
        nodesRender.runInRender {
            val surfaceTexture = com.dragon.render.texture.SurfaceTexture(2048, 1536) {
                glSurfaceView.requestRender()
            }
            val oesTextureNode = OesTextureNode(
                viewPortWidth.toFloat()/2,
                viewPortHeight.toFloat() / 2,
                viewPortWidth.toFloat(),
                0f,
                surfaceTexture!!
            )
            nodesRender.nodes.add(oesTextureNode)
            cameraHolder.selectCamera(CAMERA_FRONT).setSurface(surfaceTexture.surface)
                .startPreview().invalidate()
        }

    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
        cameraHolder.stopPreview().invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHolder.release()
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
