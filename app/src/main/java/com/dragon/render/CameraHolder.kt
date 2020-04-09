package com.dragon.render

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.concurrent.Semaphore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraHolder(private val context: Context) {
    companion object {
        const val TAG = "CameraHolder"
        const val CAMERA_REQUEST_CODE = 1

        const val CAMERA_FRONT = 1.toString()
        const val CAMERA_REAR = 0.toString()
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val handlerThread = HandlerThread("camera holder")
    private val handler: Handler
    private var invalidateBlockSemaphore = Semaphore(1, true)
    private var requestOpen = false
    private var requestPreview = false
    private var requestRestartPreview = false
    private var requestRestartOpen = false
    private var requestRelease = false

    private var cameraId: String = CAMERA_FRONT;
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private val surfaces = mutableListOf<Surface>()

    var sensorOrientation = 0
    var previewSizes = mutableListOf<Size>()

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        cameraId = cameraManager.cameraIdList.first()
    }

    fun selectCamera(id: String) = runInCameraThread {
        cameraId = id
        sensorOrientation =
            cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SENSOR_ORIENTATION)
                ?: 0
        previewSizes.clear()
        cameraManager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceTexture::class.java)?.let {
                it.forEach { size ->
                    previewSizes.add(size)
                }
            }
        previewSizes.forEach {
            Log.d(TAG, "selectCamera $it")
        }
        requestRestartOpen = true
    }

    fun getPreviewSize(block: (sizes: Array<out Size>?) -> Unit) = runInCameraThread {
        block.invoke(
            cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(SurfaceTexture::class.java)
        )
    }

    fun getRotation() = sensorOrientation

    fun setSurface(vararg vararg: Surface) = runInCameraThread {
        if (surfaces.size > 0) {
            requestRestartPreview = true
        }
        surfaces.clear()
        vararg.forEach {
            surfaces.add(it)
        }
    }

    fun open() = runInCameraThread {
        requestOpen = true
    }

    fun startPreview() = runInCameraThread {
        requestOpen = true
        requestPreview = true
    }

    fun stopPreview() = runInCameraThread {
        requestPreview = false
    }

    fun release() = runInCameraThread {
        requestPreview = false
        requestOpen = false
        requestRelease = true
    }

    fun invalidate() = runInCameraThread {
        GlobalScope.async {
            try {
                Log.d(TAG, "invalidate acquire")
                invalidateBlockSemaphore.acquire()
                Log.d(TAG, "invalidate run")

                if (cameraCaptureSession != null && (!requestPreview || requestRestartPreview || !requestOpen || requestRestartOpen)) {
                    cameraCaptureSession?.close()
                    cameraCaptureSession = null
                    Log.d(TAG, "invalidate cameraCaptureSession?.close()")
                }

                if (cameraDevice != null && (!requestOpen || requestRestartOpen || requestOpen)) {
                    cameraDevice?.close()
                    cameraDevice = null
                    Log.d(TAG, "invalidate cameraDevice?.close()")
                }

                if (cameraDevice == null && requestOpen) {
                    cameraDevice = try {
                        openCamera(cameraId)
                    } catch (e: Exception) {
                        null
                    }
                    Log.d(TAG, "invalidate openCamera() $cameraDevice")
                }

                if (cameraDevice != null && cameraCaptureSession == null && requestPreview) {
                    cameraCaptureSession = try {
                        startPreview(cameraDevice!!, surfaces)
                    } catch (e: Exception) {
                        null
                    }
                    Log.d(TAG, "invalidate startPreview() $cameraCaptureSession")
                }
                requestRestartPreview = false
                requestRestartOpen = false
            } finally {
                Log.d(TAG, "invalidate release")
                invalidateBlockSemaphore.release()
            }
        }
    }

    private fun runInCameraThread(block: CameraHolder.() -> Unit): CameraHolder {
        handler.post { block.invoke(this) }
        return this
    }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(cameraId: String) = suspendCoroutine<CameraDevice> {
        if (!checkPermission()) {
            handler.post { it.resumeWithException(RuntimeException("openCamera camera permission error")) }
            return@suspendCoroutine
        }
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                Log.d(TAG, "onOpened $camera")
                it.resume(camera)
            }

            override fun onDisconnected(camera: CameraDevice) {
                Log.d(TAG, "onDisconnected $camera")
                camera.close()
                it.resumeWithException(RuntimeException("onDisconnected"))
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Log.d(TAG, "onError $camera")
                camera.close()
                it.resumeWithException(RuntimeException("onError"))
            }

            override fun onClosed(camera: CameraDevice) {
                super.onClosed(camera)
                if (requestRelease) {
                    handlerThread.quitSafely()
                }
            }
        }, handler)
    }

    private suspend fun startPreview(cameraDevice: CameraDevice, surfaces: MutableList<Surface>) =
        suspendCoroutine<CameraCaptureSession> {
            val builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            var hasSurface = false
            surfaces.forEach { surface ->
                builder.addTarget(surface)
                if(surface.isValid){
                    hasSurface = true
                }
            }
            if(!hasSurface){
                handler.post { it.resumeWithException(Exception("surface not valid")) }
                return@suspendCoroutine
            }
            cameraDevice.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.d(TAG, "onConfigureFailed $session")
                        session.close()
                        it.resumeWithException(RuntimeException("onConfigureFailed"))
                    }

                    override fun onConfigured(session: CameraCaptureSession) {
                        Log.d(TAG, "onConfigured $session")
                        session.setRepeatingRequest(builder.build(), null, handler)
                        it.resume(session)
                    }
                },
                handler
            )
        }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
            return false
        }
        return true
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (permissions.firstOrNull() == Manifest.permission.CAMERA && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                invalidate()
            }
        }
    }
}