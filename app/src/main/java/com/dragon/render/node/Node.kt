package com.dragon.render.node

import com.dragon.render.OpenGlUtils
import com.dragon.render.program.BasicProgram
import com.dragon.render.program.ProgramFactory
import com.dragon.render.program.ProgramKey

open abstract class Node(
    var x: Float,
    var y: Float,
    var w: Float,
    var h: Float
) {
    val positionBuffer = OpenGlUtils.BufferUtils.generateFloatBuffer(8)
    val textureCoordinateBuffer = OpenGlUtils.BufferUtils.generateFloatBuffer(8)

    abstract fun render(render: NodesRender)

    abstract fun release()

    fun prepareProgram(render: NodesRender, programKey: ProgramKey): BasicProgram {
        var program = render.getProgram(programKey)
        if (program != null) {
            return program
        }
        program = ProgramFactory.create(programKey)
        render.attachProgram(program)
        return program
    }
}