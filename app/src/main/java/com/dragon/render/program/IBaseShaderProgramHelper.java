package com.dragon.render.program;


import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author chenjiulong
 */

public interface IBaseShaderProgramHelper {
	float SQUARE_VERTICES[] = {
			-1.0f, -1.0f,
			1.0f, -1.0f,
			-1.0f, 1.0f,
			1.0f, 1.0f};

	float TEXTURE_VERTICES[] = {
			0.0f, 0.0f,
			1.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 1.0f};

	FloatBuffer SQUARE_VERTICES_BUFFER = GLUtils.BufferUtils.initFloatBuffer(SQUARE_VERTICES);
	FloatBuffer TEXTURE_VERTICES_BUFFER = GLUtils.BufferUtils.initFloatBuffer(TEXTURE_VERTICES);


	short DRAW_LIST[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices
	float SQUARE_VERTICES_LIST[] = {
			-1.0f, 1.0f,
			-1.0f, -1.0f,
			1.0f, -1.0f,
			1.0f, 1.0f};
	float TEXTURE_VERTICES_LIST[] = {
			0.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f,
			1.0f, 1.0f};

	FloatBuffer SQUARE_VERTICES_BUFFER_LIST = GLUtils.BufferUtils.initFloatBuffer(SQUARE_VERTICES_LIST);
	FloatBuffer TEXTURE_VERTICES_BUFFER_LIST = GLUtils.BufferUtils.initFloatBuffer(TEXTURE_VERTICES_LIST);
	ShortBuffer DRAW_LIST_BUFFER = GLUtils.BufferUtils.initShortBuffer(DRAW_LIST);

	boolean initProgram();

	boolean renderBegin();

	boolean render(int inputTextureId, FloatBuffer squareVertices, FloatBuffer textureVertices);


	boolean render(int inputTextureId, FloatBuffer squareVertices, FloatBuffer textureVertices, ShortBuffer drawList);

	boolean renderEnd();

	boolean releaseProgram();
}
