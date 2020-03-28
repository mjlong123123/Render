package com.dragon.render.node

import com.dragon.render.program.OesTextureProgram
import com.dragon.render.program.ProgramKey
import com.dragon.render.texture.CombineSurfaceTexture

class OesTextureNode(left: Float, top: Float, right: Float, bottom: Float, val combineSurfaceTexture: CombineSurfaceTexture) :
    Node(left, top, right, bottom) {
    var program: OesTextureProgram? = null

    override fun render(render: NodesRender) {
        if (program == null) program = prepareProgram(render, ProgramKey.OES) as? OesTextureProgram
        program?.let {
            if(it.isReleased()) return
            combineSurfaceTexture.update()
            it.draw(combineSurfaceTexture.textureId,positionBuffer,combineSurfaceTexture.textureCoordinate,render.openGlMatrix.mvpMatrix)
        }
    }

    override fun release() {
    }
}