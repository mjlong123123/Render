package com.dragon.render

class OpenGlCoordinateUtils {
    companion object{
        fun generateTextureCoordinate(textureWidth:Int, textureHeight:Int,targetWidth:Int,targetHeight:Int):FloatArray{
            val containerRatio = targetWidth.toFloat() / targetHeight
            val ratio = textureWidth.toFloat() / textureHeight
            var t = 1f
            var b = 0f
            var l = 0f
            var r = 1f
            if (containerRatio > ratio) {
                t = textureWidth / containerRatio / textureHeight
            } else {
                r = textureHeight * containerRatio / textureWidth
            }
            return floatArrayOf(
                l, t,
                l, b,
                r, t,
                r, b
            )
        }
    }
}