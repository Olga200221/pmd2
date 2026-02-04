package com.example.pmd2.opengl

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object TextureHelper {

    fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun loadTexture(context: Context, resourceId: Int): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        if (textures[0] == 0) {
            android.util.Log.e("TextureHelper", "Failed to generate texture for resource $resourceId")
            return 0
        }

        val bmp = BitmapFactory.decodeResource(context.resources, resourceId)
        if (bmp == null) {
            android.util.Log.e("TextureHelper", "Failed to decode bitmap for resource $resourceId")
            return 0
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)


        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bmp, GLES20.GL_UNSIGNED_BYTE, 0)

        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            android.util.Log.e("TextureHelper", "GL error after texImage2D for $resourceId: $error")
        }

        bmp.recycle()

        android.util.Log.d("TextureHelper", "Loaded texture $resourceId with ID ${textures[0]}")
        return textures[0]
    }

    fun bindVertices(vertices: FloatArray, posHandle: Int) {
        val buffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(vertices).position(0)
        if (posHandle >= 0) {
            GLES20.glEnableVertexAttribArray(posHandle)
            GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 0, buffer)
        }
    }

    fun bindTextureCoords(coords: FloatArray, texHandle: Int) {
        val buffer: FloatBuffer = ByteBuffer.allocateDirect(coords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(coords).position(0)
        if (texHandle >= 0) {
            GLES20.glEnableVertexAttribArray(texHandle)
            GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, buffer)
        }
    }

    fun createIndexBuffer(indices: ShortArray): ShortBuffer {
        val buffer: ShortBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
        buffer.put(indices).position(0)
        return buffer
    }

    fun unbind(vararg handles: Int) {
        handles.forEach { handle ->
            if (handle >= 0) GLES20.glDisableVertexAttribArray(handle)
        }
    }
}