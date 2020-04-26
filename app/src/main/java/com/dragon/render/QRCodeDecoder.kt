package com.dragon.render

import android.graphics.ImageFormat
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.util.*

class QRCodeDecoder(w: Int, h: Int, private val callback: (String) -> Unit) : ImageReader.OnImageAvailableListener {
    private val frameThread = HandlerThread("frame thread")
    private val frameHandler: Handler
    private val imageReader: ImageReader
    private val bufferByte: ByteArray
    private var qrCodeResult: String? = null
    private val qrCodeReader = QRCodeReader()
    private val hints: Hashtable<DecodeHintType, Any> = Hashtable<DecodeHintType, Any>()

    init {
        frameThread.start()
        frameHandler = Handler(frameThread.looper)
        imageReader = ImageReader.newInstance(w, h, ImageFormat.YUV_420_888, 2)
        imageReader.setOnImageAvailableListener(this, frameHandler)
        bufferByte = ByteArray(w * h * 2)
        hints[DecodeHintType.CHARACTER_SET] = "utf-8" // 设置二维码内容的编码
        hints[DecodeHintType.POSSIBLE_FORMATS] = BarcodeFormat.QR_CODE
        Log.d("QRCodeDecoder", " w:$w h:$h")
    }

    fun reset() {
        qrCodeResult = null
    }

    fun getSurface(): Surface = imageReader.surface

    override fun onImageAvailable(reader: ImageReader?) {
        reader?.let {
            val image = it.acquireLatestImage()
            if (TextUtils.isEmpty(qrCodeResult)) {
                var offset = 0
                image.planes[0].buffer.get(bufferByte, offset, image.planes[0].buffer.limit())
                offset += image.planes[0].buffer.limit()
                image.planes[1].buffer.get(bufferByte, offset, image.planes[1].buffer.limit())
                offset += image.planes[1].buffer.limit()
                image.planes[2].buffer.get(bufferByte, offset, image.planes[2].buffer.limit())

                val source = PlanarYUVLuminanceSource(bufferByte, image.width, image.height, 0, 0, image.width, image.height, false)
                val tempBitmap = BinaryBitmap(HybridBinarizer(source))
                try {
                    qrCodeResult = qrCodeReader.decode(tempBitmap, hints)?.text
                    callback.invoke(qrCodeResult ?: "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            image.close()
        }
    }

    fun release() {
        imageReader.close()
        frameThread.quitSafely()
    }
}