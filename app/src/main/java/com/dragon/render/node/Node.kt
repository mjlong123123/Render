package com.dragon.render.node

import com.dragon.render.OpenGlUtils
import com.dragon.render.program.BasicProgram
import com.dragon.render.program.ProgramFactory
import com.dragon.render.program.ProgramKey

open abstract class Node(
    var left: Float,
    var top: Float,
    var right: Float,
    var bottom: Float
) {
    val positionBuffer = OpenGlUtils.BufferUtils.generateFloatBuffer(8)
    init {
        positionBuffer.rewind()
        positionBuffer.put(left).put(top)
        positionBuffer.put(left).put(bottom)
        positionBuffer.put(right).put(top)
        positionBuffer.put(right).put(bottom)
        positionBuffer.rewind()
    }

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