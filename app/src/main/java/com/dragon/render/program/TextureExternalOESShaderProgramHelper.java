package com.dragon.render.program;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;


import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author chenjiulong
 */

public class TextureExternalOESShaderProgramHelper implements IBaseShaderProgramHelper {
	private final String TAG = "OESShaderProgramHelper";

	private final String vertexShaderCode = "attribute vec4 vPosition;" +
			"attribute vec2 inputTextureCoordinate;" +
			"varying vec2 textureCoordinate;" +
			"void main()" +
			"{" +
			"gl_Position = vPosition;" +
			"textureCoordinate = inputTextureCoordinate;" +
			"}";

	private final String fragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n" +
			"precision mediump float;" +
			"varying vec2 textureCoordinate;\n" +
			"uniform samplerExternalOES inputImageTexture;\n" +
			"void main() {" +
			"  gl_FragColor = texture2D( inputImageTexture, textureCoordinate );\n" +
			"}";

	private int programHandle;
	private int squareHandle;
	private int textureVerticesHandle;
	private int inputTextureHandle;

	@Override
	public boolean initProgram() {
		programHandle = GLUtils.createProgram(vertexShaderCode, fragmentShaderCode);
		if (programHandle == 0) {
			Log.e(TAG, "initProgram programHandle == 0");
			return false;
		}
		squareHandle = GLES20.glGetAttribLocation(programHandle, "vPosition");
		textureVerticesHandle = GLES20.glGetAttribLocation(programHandle, "inputTextureCoordinate");
		inputTextureHandle = GLES20.glGetUniformLocation(programHandle, "inputImageTexture");
		return true;
	}

	@Override
	public boolean renderBegin() {
		if (programHandle == 0) {
			Log.e(TAG, "renderBegin programHandle == 0");
			return false;
		}
		GLES20.glUseProgram(programHandle);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glEnableVertexAttribArray(squareHandle);
		GLES20.glEnableVertexAttribArray(textureVerticesHandle);
		return true;
	}

	@Override
	public boolean render(int inputTextureId, FloatBuffer squareVertices, FloatBuffer textureVertices) {
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputTextureId);
		GLES20.glUniform1i(inputTextureHandle, 0);
		GLES20.glVertexAttribPointer(squareHandle, 2, GLES20.GL_FLOAT, false, 0, squareVertices);
		GLES20.glVertexAttribPointer(textureVerticesHandle, 2, GLES20.GL_FLOAT, false, 0, textureVertices);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		return true;
	}

	@Override
	public boolean render(int inputTextureId, FloatBuffer squareVertices, FloatBuffer textureVertices, ShortBuffer drawList) {
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputTextureId);
		GLES20.glUniform1i(inputTextureHandle, 0);
		GLES20.glVertexAttribPointer(squareHandle, 2, GLES20.GL_FLOAT, false, 0, squareVertices);
		GLES20.glVertexAttribPointer(textureVerticesHandle, 2, GLES20.GL_FLOAT, false, 0, textureVertices);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawList.limit(), GLES20.GL_UNSIGNED_SHORT, drawList);
		return true;
	}

	@Override
	public boolean renderEnd() {
		if (programHandle == 0) {
			Log.e(TAG, "renderEnd programHandle == 0");
			return false;
		}
		GLES20.glDisableVertexAttribArray(squareHandle);
		GLES20.glDisableVertexAttribArray(textureVerticesHandle);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
		GLES20.glUseProgram(0);
		return true;
	}

	@Override
	public boolean releaseProgram() {
		if (programHandle == 0) {
			Log.e(TAG, "releaseProgram programHandle == 0");
			return false;
		}
		GLES20.glDeleteProgram(programHandle);
		return true;
	}
}
