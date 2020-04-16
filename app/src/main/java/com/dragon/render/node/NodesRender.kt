package com.dragon.render.node

import android.util.SparseArray
import androidx.core.util.containsKey
import androidx.core.util.forEach
import com.dragon.render.utils.OpenGlMatrix
import com.dragon.render.program.BasicProgram
import com.dragon.render.program.ProgramKey
import com.dragon.render.texture.DoubleFrameBufferTexture
import com.dragon.render.texture.FrameBufferTexture
import java.util.concurrent.LinkedBlockingDeque

class NodesRender(
    val viewPortWidth: Int,
    val viewPortHeight: Int
) {
    private var released: Boolean = false
    private var frameBuffers: DoubleFrameBufferTexture? = null
    private val renderQueue = LinkedBlockingDeque<Runnable>()
    private val programs = SparseArray<BasicProgram>()
    private val nodes = mutableListOf<Node>()
    val openGlMatrix =
        OpenGlMatrix(viewPortWidth, viewPortHeight)

    fun render(): FrameBufferTexture? {
        //run event
        var runnable = renderQueue.poll()
        while (runnable != null) {
            runnable.run()
            runnable = renderQueue.poll()
        }
        if (released) {
            return null
        }
        //prepare double frame buffer.
        if (frameBuffers == null) {
            frameBuffers = DoubleFrameBufferTexture(viewPortWidth, viewPortHeight)
        }
        //draw nodes.
        frameBuffers!!.swap()
        nodes.forEach {
            it.render(this@NodesRender)
        }
        return frameBuffers!!.end()
    }

    fun runInRender(block: NodesRender.() -> Unit) =
        renderQueue.offer(Runnable { block.invoke(this) })

    fun addNode(node:Node){
        nodes.add(node)
    }

    fun removeNode(node: Node){
        nodes.remove(node)
    }

    fun attachProgram(program: BasicProgram) {
        programs.put(program.programKey.ordinal, program)
    }

    fun detachProgram(program: BasicProgram) {
        programs.remove(program.programKey.ordinal)
    }

    fun detachProgram(programKey: ProgramKey) {
        programs.remove(programKey.ordinal)
    }

    fun containProgram(programKey: ProgramKey): Boolean {
        return programs.containsKey(programKey.ordinal)
    }

    fun getProgram(programKey: ProgramKey) = programs.get(programKey.ordinal)

    fun release() {
        released = true
        frameBuffers?.release()
        frameBuffers = null
        nodes.forEach {
            it.release()
        }
        programs.forEach { _, program ->
            program.release()
        }
    }
}