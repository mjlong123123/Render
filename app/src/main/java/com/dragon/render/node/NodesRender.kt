package com.dragon.render.node

import android.opengl.GLES20
import android.util.SparseArray
import androidx.core.util.containsKey
import com.dragon.render.OpenGlMatrix
import com.dragon.render.OpenGlUtils
import com.dragon.render.extension.assignOpenGlPosition
import com.dragon.render.program.BasicProgram
import com.dragon.render.program.GLUtils
import com.dragon.render.program.ProgramKey
import com.dragon.render.program.TextureProgram
import com.dragon.render.texture.DoubleFrameBufferTexture
import java.util.concurrent.LinkedBlockingDeque

class NodesRender() {
    var viewPortWidth: Int = 1
    var viewPortHeight: Int = 1
    private val renderQueue = LinkedBlockingDeque<Runnable>()
    var openGlMatrix = OpenGlMatrix(viewPortWidth, viewPortHeight)
    val nodes = mutableListOf<Node>()
    private val programs = SparseArray<BasicProgram>()
    var frameBuffers = DoubleFrameBufferTexture(viewPortWidth, viewPortHeight)

    var displayProgram: TextureProgram? = null

    fun render() {
        /**
         * run event.
         */
        var runnable = renderQueue.poll()
        while (runnable != null) {
            runnable.run()
            runnable = renderQueue.poll()
        }

        //clear screen.
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        /**
         * draw nodes
         */
        frameBuffers.swap()
        nodes.forEach {
            it.render(this@NodesRender)
        }
        val frontBuffer = frameBuffers.end()

        if (displayProgram == null) {
            displayProgram = programs.get(ProgramKey.TEXTURE.ordinal) as? TextureProgram
            if (displayProgram == null) {
                displayProgram = TextureProgram()
                programs.put(displayProgram?.programKey?.ordinal!!, displayProgram)
            }
        }
        GLES20.glViewport(0, 0, viewPortWidth, viewPortHeight)
        GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        displayProgram!!.draw(
            frontBuffer.textureId,
            OpenGlUtils.BufferUtils.generateFloatBuffer(8).assignOpenGlPosition(
                0f, 0f, viewPortWidth.toFloat(), viewPortHeight.toFloat()
            ),
            frontBuffer.textureCoordinate,
            openGlMatrix.mvpMatrix
        )
    }

    fun runInRender(block: NodesRender.() -> Unit) =
        renderQueue.offer(Runnable { block.invoke(this) })

    fun updateViewPort(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        viewPortWidth = width
        viewPortHeight = height
        openGlMatrix = OpenGlMatrix(width, height)
        frameBuffers = DoubleFrameBufferTexture(viewPortWidth, viewPortHeight)
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

    }
}