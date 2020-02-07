package com.dragon.render.extension

import android.graphics.RectF

fun RectF.toTextureNormalizationPointers(): FloatArray {
    val t = bottom / height()
    val b = top / height()
    val l = left / width()
    val r = right / width()
    return floatArrayOf(
        l, t,
        l, b,
        r, t,
        r, b
    )
}

fun RectF.toTextureNormalizationPointers(container: RectF): FloatArray {
    val containerRatio = container.width() / container.height()
    val ratio = width() / height()
    var requestContainerWidth = width()
    var requestContainerHeight = height()
    var t = 1f
    var b = 0f
    var l = 0f
    var r = 1f
    if (containerRatio > ratio) {
        requestContainerHeight = width() / containerRatio
        t = requestContainerHeight / height()
    } else {
        requestContainerWidth = height() * containerRatio
        r = requestContainerWidth / width()
    }
    return floatArrayOf(
        l, t,
        l, b,
        r, t,
        r, b
    )
}