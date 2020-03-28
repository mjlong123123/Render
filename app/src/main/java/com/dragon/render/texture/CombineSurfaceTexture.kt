package com.dragon.render.texture

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.util.Log
import android.view.Surface
import com.dragon.render.OpenGlUtils

class CombineSurfaceTexture(width: Int, height: Int, oritention:Int,  notify: () -> Unit = {}) :
    BasicTexture(width, height) {
    private val surfaceTexture: SurfaceTexture
    val surface: Surface
    init {
        textureId = OpenGlUtils.createTexture()
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setDefaultBufferSize(width, height)
        surfaceTexture.setOnFrameAvailableListener { notify.invoke() }
        surface = Surface(surfaceTexture)

        val floatArray = OpenGlUtils.TextureCoordinateUtils.generateTextureCoordinate(height,width,height,width)
        Log.d("dragon_ttt","CombineSurfaceTexture ----------------------")
        floatArray.forEachIndexed{
            index, fl ->
            Log.d("dragon_ttt","CombineSurfaceTexture $index $fl")
        }
        Log.d("dragon_ttt","CombineSurfaceTexture ----------------------")
        val matrix = Matrix()
        matrix.setRotate(oritention.toFloat(), 1/2f,1/2f)
        matrix.mapPoints(floatArray)
        floatArray.forEachIndexed{
                index, fl ->
            Log.d("dragon_ttt","CombineSurfaceTexture $index $fl")
        }
        textureCoordinate.clear()
        textureCoordinate.put(floatArray)
        textureCoordinate.rewind()
    }

    fun update() {
        if(surface.isValid) {
            surfaceTexture.updateTexImage()
        }
    }

    override fun release() {
        super.release()
        surface.release()
        surfaceTexture.release()
    }
}