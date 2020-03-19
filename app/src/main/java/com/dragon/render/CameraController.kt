package com.dragon.render

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Semaphore

class CameraController(private val context: Context) {
    companion object{
        const val TAG = "CameraController"
        const val CAMERA_REQUEST_CODE = 1
    }
    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private var cameraId : String
    private var cameraDevice: CameraDevice? = null
    private var surface = mutableListOf<Surface>()
    private var cameraCaptureSession:CameraCaptureSession?= null



    private var surfaceTexture:SurfaceTexture?=null

    private lateinit var previewRequestBuilder: CaptureRequest.Builder


    private val handlerThread = HandlerThread("camera thread")
    private val handler : Handler

    private val openCameraSemaphore = Semaphore(1);
    private val  cameraStateCallback = object : CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG,"onOpened")
            cameraDevice = camera
            openCameraSemaphore.release()
            openSession()
        }

        override fun onClosed(camera: CameraDevice) {
            Log.d(TAG,"onClosed")
            cameraDevice = null
            openCameraSemaphore.release()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG,"onDisconnected")
            cameraDevice = null
            openCameraSemaphore.release()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.d(TAG,"onError")
            cameraDevice = null
            openCameraSemaphore.release()
        }
    }

    private val sessionStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.d(TAG,"CameraCaptureSession onConfigureFailed $session")
            cameraCaptureSession = null
            session.close()
        }

        override fun onConfigured(session: CameraCaptureSession) {
            Log.d(TAG,"CameraCaptureSession onConfigured $session")
            cameraCaptureSession = session
            cameraDevice?.let {
                val builder = it.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                surface.forEach { s -> builder.addTarget(s) }
                session.setRepeatingRequest(builder.build(), null, null)
            }
        }

        override fun onReady(session: CameraCaptureSession) {
            super.onReady(session)
            Log.d(TAG,"CameraCaptureSession onReady $session")
        }

        override fun onCaptureQueueEmpty(session: CameraCaptureSession) {
            super.onCaptureQueueEmpty(session)
            Log.d(TAG,"CameraCaptureSession onCaptureQueueEmpty $session")
        }

        override fun onClosed(session: CameraCaptureSession) {
            super.onClosed(session)
            Log.d(TAG,"CameraCaptureSession onClosed $session")
            cameraCaptureSession = null
            session.close()
        }

        override fun onSurfacePrepared(session: CameraCaptureSession, surface: Surface) {
            super.onSurfacePrepared(session, surface)
            Log.d(TAG,"CameraCaptureSession onSurfacePrepared $session $surface")
        }

        override fun onActive(session: CameraCaptureSession) {
            super.onActive(session)
            Log.d(TAG,"CameraCaptureSession onActive $session")
        }
    }

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        cameraId = cameraManager.cameraIdList.first()
    }

    fun release() = runInCameraThread {
        closeCamera()
        handlerThread.quit()
    }

    fun setCameraId(id: String) = runInCameraThread {
        if(id == cameraId){
            return@runInCameraThread
        }
        closeCamera()
        openCamera()
    }

    fun setSurface(vararg surface: Surface) = runInCameraThread  {
        surface.forEach {
            this.surface.add(it)
        }
        closeSession()
        openCamera()
    }

    private fun runInCameraThread( blocker: ()-> Unit){
        handler.post(blocker)
    }

    private fun openCamera(){
        checkPermission()
        try {
            if(cameraDevice != null){
                return
            }
            openCameraSemaphore.acquire()
            cameraManager.openCamera(cameraId, cameraStateCallback, handler)
        }finally {
            openCameraSemaphore.release()
        }
    }

    private fun openSession(){
        cameraDevice?.createCaptureSession(surface, sessionStateCallback, handler)
    }

    private fun closeSession(){
        cameraCaptureSession?.close()
        cameraCaptureSession = null
    }

    private fun closeCamera() {
        closeSession()
        try {
            openCameraSemaphore.acquire()
            cameraDevice?.close()
            cameraDevice = null
        } finally {
            openCameraSemaphore.release()
        }
    }

    private fun checkPermission():Boolean{
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
    ){
        if(requestCode == CAMERA_REQUEST_CODE){
            if(permissions.firstOrNull() == Manifest.permission.CAMERA && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED){
                runInCameraThread { openCamera() }
            }
        }
    }
}