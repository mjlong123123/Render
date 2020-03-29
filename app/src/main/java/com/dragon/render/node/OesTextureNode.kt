package com.dragon.render.node

import com.dragon.render.extension.assignOpenGLTextureCoordinate
import com.dragon.render.extension.assignOpenGlPosition
import com.dragon.render.program.OesTextureProgram
import com.dragon.render.program.ProgramKey
import com.dragon.render.texture.CombineSurfaceTexture

class OesTextureNode(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    val combineSurfaceTexture: CombineSurfaceTexture
) :
    Node(x, y, w, h) {
    var program: OesTextureProgram? = null

    init {
        positionBuffer.assignOpenGlPosition(
            x,
            y,
            w,
            h
        )
        textureCoordinateBuffer.assignOpenGLTextureCoordinate(
            combineSurfaceTexture.width.toFloat(),
            combineSurfaceTexture.height.toFloat(),
            w,
            h,
            combineSurfaceTexture.orientation.toFloat(),
            flipX = true
        )
    }

    override fun render(render: NodesRender) {
        if (program == null) program = prepareProgram(render, ProgramKey.OES) as? OesTextureProgram
        program?.let {
            if (it.isReleased()) return
            combineSurfaceTexture.update()
            it.draw(
                combineSurfaceTexture.textureId,
                positionBuffer,
                textureCoordinateBuffer,
                render.openGlMatrix.mvpMatrix
            )
        }
    }

    override fun release() {
    }
}