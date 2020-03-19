/*
* GLUtils.java $version 2016. 12. 05.
*
* Copyright 2016 LINE Corporation. All rights Reserved. 
* LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
*/
package com.dragon.render.program;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author dragon
 */
public class GLUtils {

	public static final float SQUARE_COORDINATES[] = {
			-1.0f, -1.0f,
			1.0f, -1.0f,
			-1.0f, 1.0f,
			1.0f, 1.0f};

	public static final float TEXTURE_VERTICES[] = {
			0.0f, 0.0f,
			1.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 1.0f};

	public static final FloatBuffer SQUARE_COORDINATES_BUFFER = GLUtils.BufferUtils.initFloatBuffer(GLUtils.SQUARE_COORDINATES);
	public static final FloatBuffer TEXTURE_VERTICES_BUFFER = GLUtils.BufferUtils.initFloatBuffer(GLUtils.TEXTURE_VERTICES);


	public static int[] createFrameBuffer(int width, int height) {
		int[] frameBufferIds = new int[1];
		int[] textureIds = new int[1];

		GLES20.glGenFramebuffers(1, frameBufferIds, 0);
		//generate texture.
		GLES20.glGenTextures(1, textureIds, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
			Log.e("GroupFrameBuffer", "Error creating frame buffer " + frameBufferIds[0] + " " + textureIds[0]);
		}
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferIds[0]);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureIds[0], 0);

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glFlush();

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

		return new int[]{frameBufferIds[0], textureIds[0]};
	}

	public static void bindFrameBuffer(int frameBufferId, int frameBufferTextureId) {
		if (frameBufferId < 0 || frameBufferTextureId < 0) {
			return;
		}
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, frameBufferTextureId, 0);
	}


	public static void unbindFrameBuffer() {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
	}

	public static void destroyFramebuffer(int frameBufferId, int frameBufferTextureId) {
		if (frameBufferTextureId > 0) {
			GLES20.glDeleteTextures(1, new int[]{frameBufferTextureId}, 0);
		}
		if (frameBufferId > 0) {
			GLES20.glDeleteFramebuffers(1, new int[]{frameBufferTextureId}, 0);
		}
	}


	public static int createTextureID() {
		int[] texture = new int[1];
		GLES20.glGenTextures(1, texture, 0);
//		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
//		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
//		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
//		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
//		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		return texture[0];
	}

	/**
	 * @param vertexShader
	 * @param fragmentShader
	 * @return
	 */
	public static final int createProgram(String vertexShader, String fragmentShader) {

		int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
		int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
		if (vertex == 0 || fragment == 0) {
			Log.d("Load Program", "load error vertex:"+vertex);
			Log.d("Load Program", "load error fragmentShader:"+fragmentShader);
			return 0;
		}

		int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertex);
		GLES20.glAttachShader(program, fragment);
		GLES20.glLinkProgram(program);
		int[] link = new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link, 0);
		if (link[0] <= 0) {
			Log.d("Load Program", "Linking Failed");
			Log.d("Load Program", "Linking vertex:"+vertex);
			Log.d("Load Program", "Linking fragmentShader:"+fragmentShader);
			GLES20.glDeleteProgram(program);
			program = 0;
		}

		GLES20.glDeleteShader(vertex);
		GLES20.glDeleteShader(fragment);
		return program;
	}

	public static final void deleteProgram(int program) {
		GLES20.glDeleteProgram(program);
	}

	/**
	 * @param type       GLES20.GL_VERTEX_SHADER   GLES20.GL_FRAGMENT_SHADER
	 * @param shaderCode program code.
	 * @return program id.
	 */
	private static final int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		int[] compiled = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			final String glInfoLog = GLES20.glGetShaderInfoLog(shader);
			Log.d("Load Shader Failed", "Compilation\n" + glInfoLog);
			return 0;
		}
		return shader;
	}


	public static final int loadBitmapTexture(Bitmap bitmap) {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		return textures[0];
	}

	public static final class ArrayUtils{
		public static final String TAG = "ArrayUtils";

		private static final short DRAW_LIST_SIZE_STEP = 6;
		private static final short DRAW_LIST_INDEX_STEP = 4;

		public static final short [] createDrawOrderArray(int pointCount){
			if(pointCount <= 0){
				Log.e(TAG,"createDrawOrderArray pointCount "+pointCount+" error!!!");
				return null;
			}

			short [] drawListArray = new short[pointCount * DRAW_LIST_SIZE_STEP];
			for (short i = 0; i < pointCount; i++) {
				//0,1,2
				drawListArray[i * DRAW_LIST_SIZE_STEP] = (short) (i * DRAW_LIST_INDEX_STEP);
				drawListArray[i * DRAW_LIST_SIZE_STEP + 1] = (short) (i * DRAW_LIST_INDEX_STEP + 1);
				drawListArray[i * DRAW_LIST_SIZE_STEP + 2] = (short) (i * DRAW_LIST_INDEX_STEP + 2);
				//0,2,3
				drawListArray[i * DRAW_LIST_SIZE_STEP + 3] = (short) (i * DRAW_LIST_INDEX_STEP);
				drawListArray[i * DRAW_LIST_SIZE_STEP + 4] = (short) (i * DRAW_LIST_INDEX_STEP + 2);
				drawListArray[i * DRAW_LIST_SIZE_STEP + 5] = (short) (i * DRAW_LIST_INDEX_STEP + 3);
			}
			return drawListArray;
		}

		public static final short [] removeDrawOrderArray(short[] buffer, int removePointIndex) {
			if (buffer == null) {
				Log.e(TAG, "removeDrawOrderArray buffer == null ");
				return null;
			}

			int bufferLength = buffer.length;
			if (bufferLength % DRAW_LIST_SIZE_STEP != 0) {
				Log.e(TAG, "removeDrawOrderArray bufferLength " + bufferLength);
				return null;
			}

			int pointsCount = bufferLength / DRAW_LIST_SIZE_STEP;
			if (removePointIndex < 0 || removePointIndex >= pointsCount) {
				Log.e(TAG, "removeDrawOrderArray removePointIndex " + removePointIndex);
				Log.e(TAG, "removeDrawOrderArray pointsCount " + pointsCount);
				return null;
			}

			short[] ret = new short[bufferLength - 1];
			if (removePointIndex == 0) {
				System.arraycopy(buffer, DRAW_LIST_SIZE_STEP, ret, 0, ret.length);
			} else if (removePointIndex == (bufferLength - 1)) {
				System.arraycopy(buffer, 0, ret, 0, ret.length);
			} else {
				System.arraycopy(buffer, 0, ret, 0, removePointIndex * DRAW_LIST_SIZE_STEP);
				System.arraycopy(buffer, (removePointIndex + 1) * DRAW_LIST_SIZE_STEP, ret, removePointIndex * DRAW_LIST_SIZE_STEP, ret.length - (removePointIndex * DRAW_LIST_SIZE_STEP));
			}
			return ret;
		}
	}

	public static final class BufferUtils {
		public static final String TAG = "BufferUtils";

		public static final FloatBuffer initFloatBuffer(float[] inputBuffer) {
			if (inputBuffer == null || inputBuffer.length == 0) {
				Log.e(TAG,"initFloatBuffer error!!! inputBuffer null!!");
				return null;
			}
			FloatBuffer ret;
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(inputBuffer.length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			ret = byteBuffer.asFloatBuffer();
			ret.put(inputBuffer);
			ret.position(0);
			return ret;
		}

		public static final FloatBuffer initFloatBuffer(float[] inputBuffer, int count) {
			if (inputBuffer == null || inputBuffer.length == 0) {
				Log.e(TAG,"initFloatBuffer error!!! inputBuffer null!!");
				return null;
			}
			FloatBuffer ret;
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(inputBuffer.length * 4 * count);
			byteBuffer.order(ByteOrder.nativeOrder());
			ret = byteBuffer.asFloatBuffer();
			for(int i = 0; i < count; i++){
				ret.put(inputBuffer);
			}
			ret.position(0);
			return ret;
		}

		public static final FloatBuffer initFloatBuffer(float[][][] inputBuffer) {
			if (inputBuffer == null || inputBuffer.length == 0) {
				Log.e(TAG,"initFloatBuffer error!!! inputBuffer null!!");
				return null;
			}
			FloatBuffer ret;
			int length = inputBuffer.length * inputBuffer[0].length * inputBuffer[0][0].length;
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			ret = byteBuffer.asFloatBuffer();

			for (int i = 0; i < inputBuffer.length; i++) {
				for (int j = 0; j < inputBuffer[0].length; j++) {
					ret.put(inputBuffer[i][j]);
				}
			}

			ret.position(0);
			return ret;
		}


		public static final ShortBuffer initShortBuffer(short[] inputBuffer) {
			if (inputBuffer == null || inputBuffer.length == 0) {
				Log.e(TAG,"initShortBuffer error!!! inputBuffer null!!");
				return null;
			}
			ShortBuffer ret;
			ByteBuffer dlb = ByteBuffer.allocateDirect(inputBuffer.length * 2);
			dlb.order(ByteOrder.nativeOrder());
			ret = dlb.asShortBuffer();
			ret.put(inputBuffer);
			ret.position(0);
			return ret;
		}
	}




	private static Matrix convertLT2LBMatrix;

	/**
	 * convert position to -1~1 range.
	 * @param inArray
	 * @param outArray
	 * @param width
	 * @param height
	 * @return
	 */
	public static final float[] convertLT2LB(float [] inArray, float [] outArray, float width, float height) {
		if (outArray == null) {
			outArray = new float[inArray.length];
		}
		if (convertLT2LBMatrix == null) {
			convertLT2LBMatrix = new Matrix();
			convertLT2LBMatrix.setScale(1.0f, -1.0f);
		}
		int count = inArray.length / 2;
		for (int pi = 0; pi < count; pi++) {
			float x = inArray[2 * pi + 0];
			float y = inArray[2 * pi + 1];
			outArray[pi * 2 + 0] = (x / width) * 2.0f - 1.0f;
			outArray[pi * 2 + 1] = (y / height) * 2.0f - 1.0f;
		}
		convertLT2LBMatrix.mapPoints(outArray);
		return outArray;
	}

	public static final float[] convertCoordinateLT2LB(float [] inArray, float [] outArray, float width, float height) {
		if (outArray == null) {
			outArray = new float[inArray.length];
		}
		if (convertLT2LBMatrix == null) {
			convertLT2LBMatrix = new Matrix();
			convertLT2LBMatrix.setScale(1.0f, -1.0f);
		}
		int count = inArray.length / 2;
		for (int pi = 0; pi < count; pi++) {
			float x = inArray[2 * pi + 0];
			float y = inArray[2 * pi + 1];
			outArray[pi * 2 + 0] = (x / width);
			outArray[pi * 2 + 1] = (y / height);
		}
		convertLT2LBMatrix.mapPoints(outArray);
		return outArray;
	}

	/**
	 * generate point vertices via points coordinates.
	 * @param inPoints
	 * @param pointWidth
	 * @param pointHeight
	 * @return
	 */
	public static final FloatBuffer generatePointPosition(float [] inPoints, float pointWidth, float pointHeight, float width, float height) {
		final float[] convertedPoints = GLUtils.convertLT2LB(inPoints, null, width, height);
		float[] temp = new float[convertedPoints.length * 4];
		for (int i = 0; i < (convertedPoints.length / 2); i++) {
			float x = convertedPoints[i * 2];
			float y = convertedPoints[i * 2 + 1];

			temp[i * 8] = x - pointWidth;
			temp[i * 8 + 1] = y + pointHeight;

			temp[i * 8 + 2] = x - pointWidth;
			temp[i * 8 + 3] = y - pointHeight;

			temp[i * 8 + 4] = x + pointWidth;
			temp[i * 8 + 5] = y - pointHeight;

			temp[i * 8 + 6] = x + pointWidth;
			temp[i * 8 + 7] = y + pointHeight;
		}
		return GLUtils.BufferUtils.initFloatBuffer(temp);
	}

	public static final void updatePointPosition(float [] points,int index, float offsetX, float offsetY){
		if(points == null){
			return;
		}
		int startIndex = index * 2;
		if(startIndex < points.length){
			points[startIndex] = points[startIndex] + offsetX;
			points[startIndex + 1] = points[startIndex + 1] + offsetY;
		}
	}

	public static final void updatePointPosition(FloatBuffer positionsBuffer, int index, float offsetX, float offsetY){
		if(positionsBuffer == null){
			return;
		}
		int startIndex = index * 8;
		if(startIndex < positionsBuffer.limit()){
			positionsBuffer.put(startIndex + 0, positionsBuffer.get(startIndex + 0) + offsetX);
			positionsBuffer.put(startIndex + 1, positionsBuffer.get(startIndex + 1) + offsetY);

			positionsBuffer.put(startIndex + 2, positionsBuffer.get(startIndex + 2) + offsetX);
			positionsBuffer.put(startIndex + 3, positionsBuffer.get(startIndex + 3) + offsetY);

			positionsBuffer.put(startIndex + 4, positionsBuffer.get(startIndex + 4) + offsetX);
			positionsBuffer.put(startIndex + 5, positionsBuffer.get(startIndex + 5) + offsetY);

			positionsBuffer.put(startIndex + 6, positionsBuffer.get(startIndex + 6) + offsetX);
			positionsBuffer.put(startIndex + 7, positionsBuffer.get(startIndex + 7) + offsetY);
		}
	}

	private static final short DRAW_LIST_SIZE_STEP = 6;
	private static final short DRAW_LIST_INDEX_STEP = 4;

	/**
	 * generate draw order list for points.
	 * @param pointCount
	 * @return
	 */
	public static final ShortBuffer generateDrawListCCW(int pointCount) {
		short[] drawListArray = new short[pointCount * DRAW_LIST_SIZE_STEP];
		for (short i = 0; i < pointCount; i++) {
			//0,1,2
			drawListArray[i * DRAW_LIST_SIZE_STEP] = (short) (i * DRAW_LIST_INDEX_STEP);
			drawListArray[i * DRAW_LIST_SIZE_STEP + 1] = (short) (i * DRAW_LIST_INDEX_STEP + 1);
			drawListArray[i * DRAW_LIST_SIZE_STEP + 2] = (short) (i * DRAW_LIST_INDEX_STEP + 2);
			//0,2,3
			drawListArray[i * DRAW_LIST_SIZE_STEP + 3] = (short) (i * DRAW_LIST_INDEX_STEP);
			drawListArray[i * DRAW_LIST_SIZE_STEP + 4] = (short) (i * DRAW_LIST_INDEX_STEP + 2);
			drawListArray[i * DRAW_LIST_SIZE_STEP + 5] = (short) (i * DRAW_LIST_INDEX_STEP + 3);
		}
		return GLUtils.BufferUtils.initShortBuffer(drawListArray);
	}

	/**
	 * update point draw CW
	 * @param drawList
	 * @param index
	 */
	public static  final void updateDrawListCW(ShortBuffer drawList, int index) {
		if(drawList == null){
			return;
		}
		int startIndex = index * DRAW_LIST_SIZE_STEP;
		if (startIndex < drawList.limit()) {
			short va0 = drawList.get(startIndex + 0);
			drawList.put(startIndex + 1, (short) (va0 + 2));
			drawList.put(startIndex + 2, (short) (va0 + 1));
			drawList.put(startIndex + 4, (short) (va0 + 3));
			drawList.put(startIndex + 5, (short) (va0 + 2));
		}
	}

	/**
	 * update point draw CCW
	 * @param drawList
	 * @param index
	 */
	public static  final void updateDrawListCCW(ShortBuffer drawList, int index) {
		int startIndex = index * DRAW_LIST_SIZE_STEP;
		if (startIndex < drawList.limit()) {
			short va0 = drawList.get(startIndex + 0);
			drawList.put(startIndex + 1, (short) (va0 + 1));
			drawList.put(startIndex + 2, (short) (va0 + 2));
			drawList.put(startIndex + 4, (short) (va0 + 2));
			drawList.put(startIndex + 5, (short) (va0 + 3));
		}
	}

	public static final FloatBuffer generatePointTexturesCoord(int pointCount, float [] textureCoordinate){
		return GLUtils.BufferUtils.initFloatBuffer(textureCoordinate,pointCount);
	}
}
