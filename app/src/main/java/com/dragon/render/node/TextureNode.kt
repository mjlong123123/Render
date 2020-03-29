package com.dragon.render.node

import com.dragon.render.extension.assignOpenGLTextureCoordinate
import com.dragon.render.extension.assignOpenGlPosition
import com.dragon.render.program.ProgramKey
import com.dragon.render.program.TextureProgram
import com.dragon.render.texture.BitmapTexture

class TextureNode(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    private val bitmapTexture: BitmapTexture
) : Node(x, y, w, h) {
    var program: TextureProgram? = null

    init {
        positionBuffer.assignOpenGlPosition(x, y, w, h)
        textureCoordinateBuffer.assignOpenGLTextureCoordinate(
            bitmapTexture.width.toFloat(),
            bitmapTexture.height.toFloat(),
            w,
            h,
            0f,
            flipX = false,
            flipY = true
        )
    }

    override fun render(render: NodesRender) {
        if (program == null) program = prepareProgram(render, ProgramKey.TEXTURE) as? TextureProgram
        program?.let {
            if (it.isReleased()) return
            it.draw(
                bitmapTexture.textureId,
                positionBuffer,
                textureCoordinateBuffer,
                render.openGlMatrix.mvpMatrix
            )
        }
    }

    override fun release() {
    }
}