package com.dragon.render.node

import com.dragon.render.program.ProgramKey
import com.dragon.render.program.TextureProgram
import com.dragon.render.texture.BitmapTexture

class TextureNode(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    private val bitmapTexture: BitmapTexture
) : Node(left, top, right, bottom) {
    var program: TextureProgram? = null

    init {
        bitmapTexture.crop(right.toInt() - left.toInt(), top.toInt() - bottom.toInt())
    }

    override fun render(render: NodesRender) {
        if (program == null) program = prepareProgram(render, ProgramKey.TEXTURE) as? TextureProgram
        program?.let {
            if (it.isReleased()) return
            it.draw(
                bitmapTexture.textureId,
                positionBuffer,
                bitmapTexture.textureCoordinate,
                render.openGlMatrix.mvpMatrix
            )
        }
    }

    override fun release() {
    }
}