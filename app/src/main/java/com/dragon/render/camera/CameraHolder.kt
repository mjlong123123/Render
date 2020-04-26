package com.dragon.render.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.app.ActivityCompat
import java.util.concurrent.CountDownLatch

class CameraHolder(private val context: Context) {
    companion object {
        const val TAG = "CameraHolder"
        const val CAMERA_REQUEST_CODE = 1

        const val CAMERA_FRONT = 1.toString()
        const val CAMERA_REAR = 0.toString()
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val handlerThread = HandlerThread("camera holder")
    private var handler: Handler?
    private var requestOpen = false
    private var requestPreview = false
    private var requestRestartPreview = false
    private var requestRestartOpen = false
    private var requestRelease = false

    var sensorOrientation = 0
    val previewSizes = mutableListOf<Size>()
    val surfaces = mutableListOf<Surface>()
    var cameraId: String = CAMERA_FRONT
        set(value) {
            field = value
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val streamConfig = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = streamConfig?.getOutputSizes(SurfaceTexture::class.java)
            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            previewSizes.clear()
            sizes?.forEach {
                previewSizes.add(it)
            }
            runInCameraThread {
                if (cameraDevice != null) {
                    requestRestartOpen = true
                }
            }
        }
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        cameraId = cameraManager.cameraIdList.first()
    }

    fun setSurface(vararg vararg: Surface?) = runInCameraThread {
        if (cameraCaptureSession != null) {
            requestRestartPreview = true
        }
        surfaces.clear()
        vararg.forEach {
            if (it != null) {
                surfaces.add(it)
            }
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

    fun close() = runInCameraThread {
        requestPreview = false
        requestOpen = false
    }

    fun release() = runInCameraThread {
        close()
        requestRelease = true
    }

    fun invalidate() = runInCameraThread {
        if (cameraCaptureSession != null && (!requestPreview || requestRestartPreview || !requestOpen || requestRestartOpen)) {
            cameraCaptureSession?.close()
            cameraCaptureSession = null
            requestRestartPreview = false
            Log.d(TAG, "invalidate cameraCaptureSession?.close()")
        }

        if (cameraDevice != null && (!requestOpen || requestRestartOpen)) {
            cameraDevice?.close()
            cameraDevice = null
            requestRestartOpen = false
            Log.d(TAG, "invalidate cameraDevice?.close()")
        }

        if (cameraDevice == null && requestOpen) {
            Log.d(TAG, "invalidate openCamera()")
            openCameraInternal()
        }

        if (cameraDevice != null && cameraCaptureSession == null && requestPreview) {
            Log.d(TAG, "invalidate startPreview()")
            startPreviewInternal()
        }

        if (requestRelease) {
            Log.d(TAG, "invalidate release()")
            handler = null
            handlerThread.quitSafely()
        }
    }

    private fun runInCameraThread(block: CameraHolder.() -> Unit): CameraHolder {
        handler?.post { block.invoke(this) }
        return this
    }

    @SuppressLint("MissingPermission")
    private fun openCameraInternal() {
        if (!checkPermission()) {
            Log.d(TAG, "openCamera !checkPermission()")
            return
        }
        try {
            Log.d(TAG, "openCamera")
            if (cameraDevice != null) {
                Log.d(TAG, "openCamera cameraDevice != null")
                return
            }
            val countDownLatch = CountDownLatch(1);
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d(TAG, "openCamera onOpened $camera")
                    cameraDevice = camera
                    countDownLatch.countDown()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d(TAG, "openCamera onDisconnected $camera")
                    camera.close()
                    cameraDevice = null
                    countDownLatch.countDown()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.d(TAG, "openCamera onError $camera")
                    camera.close()
                    cameraDevice = null
                    countDownLatch.countDown()
                }

                override fun onClosed(camera: CameraDevice) {
                    Log.d(TAG, "openCamera onClosed $camera")
                    cameraDevice = null
                }
            }, Handler(Looper.getMainLooper()))
            Log.d(TAG, "openCamera await")
            countDownLatch.await()
            Log.d(TAG, "openCamera go on")
        } catch (ae: CameraAccessException) {
            Log.d(TAG, "openCamera CameraAccessException $ae")
            cameraDevice = null
        } catch (ie: InterruptedException) {
            Log.d(TAG, "openCamera CameraAccessException $ie")
        }
    }

    private fun startPreviewInternal() {
        Log.d(TAG, "startPreview cameraDevice $cameraDevice surfaces $surfaces")
        cameraDevice?.let { camera ->
            try {
                val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                var hasSurface = false
                surfaces.forEach { surface ->
                    builder.addTarget(surface)
                    if (surface.isValid) {
                        hasSurface = true
                    }
                }
                if (!hasSurface) {
                    Log.d(TAG, "startPreview have not surface.")
                    return
                }
                val countDownLatch = CountDownLatch(1)
                camera.createCaptureSession(
                    surfaces,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.d(TAG, "startPreview session onConfigureFailed $session")
                            session.close()
                            cameraCaptureSession = null
                            countDownLatch.countDown()
                        }

                        override fun onConfigured(session: CameraCaptureSession) {
                            Log.d(TAG, "startPreview session onConfigured $session")
                            cameraCaptureSession = session
                            try {
                                session.setRepeatingRequest(builder.build(), null, handler)
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                            countDownLatch.countDown()
                        }

                        override fun onClosed(session: CameraCaptureSession) {
                            Log.d(TAG, "startPreview session onClosed $session")
                            cameraCaptureSession = null
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
                Log.d(TAG, "startPreview await")
                countDownLatch.await()
                Log.d(TAG, "startPreview go on")
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
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