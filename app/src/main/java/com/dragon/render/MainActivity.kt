package com.dragon.render

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dragon.render.camera.CameraHolder
import com.dragon.render.camera.CameraHolder.Companion.CAMERA_FRONT
import com.dragon.render.camera.CameraHolder.Companion.CAMERA_REAR
import com.dragon.render.node.NodesRender
import com.dragon.render.node.OesTextureNode
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

    private var qrCodeDecoder: QRCodeDecoder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        customRender = CustomRender(glSurfaceView, nodesRender)
        nodesRender.runInRender {
            var size = cameraHolder.previewSizes.first { size -> size.width <= 1920 && size.height <= 1920 }
            updatePreviewNode(
                size.width,
                size.height
            )
            size = cameraHolder.previewSizes.first { size -> size.width <= 640 && size.height <= 640 }
            qrCodeDecoder?.release()
            qrCodeDecoder = QRCodeDecoder(size.width, size.height) { ret ->
                showQRCodeResult(ret)
            }
            cameraHolder.setSurface(
                cameraPreviewNode!!.combineSurfaceTexture.surface,
                qrCodeDecoder?.getSurface()
            ).invalidate()
        }

        switchCamera.setOnClickListener {
            nodesRender.runInRender {
                val cameraId = when (cameraHolder.cameraId) {
                    CAMERA_REAR -> CAMERA_FRONT
                    else -> CAMERA_REAR
                }
                cameraHolder.cameraId = cameraId
                var size = cameraHolder.previewSizes.first { size -> size.width <= 1280 && size.height <= 1280 }
                updatePreviewNode(
                    size.width,
                    size.height
                )
                cameraHolder.setSurface(cameraPreviewNode!!.combineSurfaceTexture.surface, qrCodeDecoder?.getSurface())
                    .invalidate()
            }
        }
        previewSize.setOnClickListener {
            cameraHolder?.let {
                it.previewSizes?.let { sizes ->
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
                            cameraHolder.setSurface(cameraPreviewNode!!.combineSurfaceTexture.surface, qrCodeDecoder?.getSurface())
                                .invalidate()
                        }
                    }
                    builder.create().show()
                }
            }
        }
        cameraHolder.cameraId = CAMERA_REAR
        cameraHolder.open().invalidate()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
        cameraHolder.startPreview().invalidate()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
        cameraHolder.stopPreview().invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHolder.release().invalidate()
        qrCodeDecoder?.release()
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
            nodesRender.viewPortHeight.toFloat(),
            surfaceTexture!!
        )
    }

    private fun showQRCodeResult(ret: String) {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("OK") { _, _ ->
            qrCodeDecoder?.reset()
        }
        builder.setMessage(ret)
        builder.show()
    }
}
