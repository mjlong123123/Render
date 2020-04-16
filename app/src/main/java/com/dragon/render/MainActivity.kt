package com.dragon.render

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    private var cameraPreviewNode: OesTextureNode? = null
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
                viewPortWidth.toFloat() / 2,
                viewPortHeight.toFloat() / 2,
                viewPortWidth.toFloat(),
                viewPortHeight.toFloat(),
                bitmapTexture
            )
            nodesRender.addNode(textureNode)
        }
        nodesRender.runInRender {
            updatePreviewNode(
                cameraHolder.previewSizes.first().width,
                cameraHolder.previewSizes.first().height
            )
            cameraHolder.setSurface(cameraPreviewNode!!.combineSurfaceTexture.surface)
                .requestPreview().invalidate()
        }
        cameraHolder.selectCamera(CAMERA_FRONT)
        switchCamera.setOnClickListener {
            nodesRender.runInRender {
                val cameraId = when (cameraHolder.cameraId) {
                    CAMERA_REAR -> CAMERA_FRONT
                    else -> CAMERA_REAR
                }
                cameraHolder.selectCamera(cameraId)
                updatePreviewNode(
                    cameraHolder.previewSizes.first().width,
                    cameraHolder.previewSizes.first().height
                )
                cameraHolder.setSurface(cameraPreviewNode!!.combineSurfaceTexture.surface)
                    .invalidate()
            }
        }
        previewSize.setOnClickListener {
            cameraHolder?.let {
                it.getPreviewSize { sizes ->
                    val builder = AlertDialog.Builder(this@MainActivity)
                    val sizesString: Array<String> = Array(sizes?.size ?: 0) { "" }
                    sizes?.forEachIndexed { index, item ->
                        sizesString[index] = item.width.toString() + "*" + item.height.toString()
                    }
                    builder.setItems(sizesString) { d, index ->
                        val size = sizesString[index].split("*")
                        val width = size[0].toInt()
                        val height = size[1].toInt()

                        nodesRender.runInRender {
                            updatePreviewNode(width, height)
                            cameraHolder.setSurface(cameraPreviewNode!!.combineSurfaceTexture.surface)
                                .invalidate()
                        }
                    }
                    builder.create().show()
                }
            }
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

    private fun updatePreviewNode(w: Int, h: Int) {
        cameraPreviewNode?.let {
            nodesRender.removeNode(it)
            it.release()
        }
        cameraPreviewNode = generatePreviewNode(w, h)
        nodesRender.addNode(cameraPreviewNode!!)
    }

    private fun generatePreviewNode(w: Int, h: Int): OesTextureNode {
        val windowRotation = (this@MainActivity).windowManager.defaultDisplay.rotation
        val cameraRotation = cameraHolder.sensorOrientation
        val rotation = (cameraRotation - windowRotation).absoluteValue

        val surfaceTexture =
            CombineSurfaceTexture(w, h, rotation, cameraHolder.cameraId == CAMERA_REAR) {
                glSurfaceView.requestRender()
            }
        return OesTextureNode(
            0f,
            0f,
            nodesRender.viewPortWidth.toFloat(),
            800f,
            surfaceTexture!!
        )
    }
}
