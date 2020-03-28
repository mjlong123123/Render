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
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.RuntimeException
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
    private var invalidateJob: Job? = null
    private var opened = false
    private var previewed = false
    private var previewAgain = false
    private var openAgain = false
    private var released = false

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

    fun selectCamera(id: String): CameraHolder {
        cameraManager.cameraIdList.forEach {
            Log.d(TAG, "selectCamera $it")
        }
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
        Log.d(TAG,"selectCamera sensorOrientation $sensorOrientation")
        Log.d(TAG,"selectCamera window rotation ${(context as FragmentActivity).windowManager.defaultDisplay.rotation}")
        previewSizes.forEach {
            Log.d(TAG,"selectCamera $it")
        }
        openAgain = true
        return this
    }

    fun getPreviewSize(): Array<out Size>? {
        return cameraManager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceTexture::class.java)
    }

    fun selectPreviewSize(): CameraHolder {
        val characterMap = cameraManager.getCameraCharacteristics(cameraId)
        val sizes = characterMap.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceTexture::class.java)
        sizes?.let {
            sizes.forEach { size ->
                Log.d(TAG, "selectPreviewSize size $size")
            }
        }
        return this
    }

    fun setSurface(vararg vararg: Surface): CameraHolder {
        if (surfaces.size > 0) {
            previewAgain = true
        }
        surfaces.clear()
        vararg.forEach {
            surfaces.add(it)
        }
        return this
    }

    fun open(): CameraHolder {
        if (checkPermission()) {
            opened = true
        }
        return this
    }

    fun startPreview(): CameraHolder {
        if (checkPermission()) {
            opened = true
            previewed = true
        }
        return this
    }

    fun stopPreview(): CameraHolder {
        previewed = false
        return this
    }


    fun release(): CameraHolder {
        previewed = false
        opened = false
        released = true
        invalidate()
        return this
    }

    fun invalidate() {
        if (invalidateJob != null) {
            throw RuntimeException("invalidate invalidateJob is running!")
        }
        invalidateJob = GlobalScope.launch {

            if (cameraCaptureSession != null && (!previewed || previewAgain)) {
                cameraCaptureSession?.close()
                cameraCaptureSession = null
                Log.d(TAG, "invalidate cameraCaptureSession?.close()")
            }

            if (cameraDevice != null && (!opened || openAgain)) {
                cameraCaptureSession?.close()
                cameraCaptureSession = null
                cameraDevice?.close()
                cameraDevice = null
                Log.d(TAG, "invalidate cameraDevice?.close()")
            }

            if (cameraDevice == null && opened) {
                cameraDevice = try {
                    openCamera(cameraId)
                } catch (e: Exception) {
                    null
                }
                Log.d(TAG, "invalidate openCamera() $cameraDevice")
            }

            if (cameraDevice != null && cameraCaptureSession == null && previewed) {
                cameraCaptureSession = try {
                    startPreview(cameraDevice!!, surfaces)
                } catch (e: Exception) {
                    null
                }
                Log.d(TAG, "invalidate startPreview() $cameraCaptureSession")
            }
            previewAgain = false
            openAgain = false
            if (released) {
                handlerThread.quitSafely()
            }
            invalidateJob = null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(cameraId: String) = suspendCoroutine<CameraDevice> {
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
        }, handler)
    }

    private suspend fun startPreview(cameraDevice: CameraDevice, surfaces: MutableList<Surface>) =
        suspendCoroutine<CameraCaptureSession> {
            val builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            surfaces.forEach { surface ->
                builder.addTarget(surface)
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