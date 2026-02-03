package com.example.pmd2.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object ShaderProgram {

    private var programId = 0
    private var posLoc = 0
    private var texLoc = 0
    private var mvpMatrixLoc = 0
    private var samplerLoc = 0

    init {
        // Шейдеры создаем один раз для всех сфер
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            varying vec2 vTexCoord;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        posLoc = GLES20.glGetAttribLocation(programId, "vPosition")
        texLoc = GLES20.glGetAttribLocation(programId, "aTexCoord")
        mvpMatrixLoc = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        samplerLoc = GLES20.glGetUniformLocation(programId, "uTexture")
    }

    // Данные сферы
    data class SphereData(val vertices: FloatArray, val indices: ShortArray)

    // Генерация геометрии сферы
    fun createSphereData(radius: Float, stacks: Int, slices: Int): SphereData {
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        for (i in 0..stacks) {
            val lat = Math.PI / 2 - i * Math.PI / stacks
            val sinLat = Math.sin(lat)
            val cosLat = Math.cos(lat)

            for (j in 0..slices) {
                val lon = 2 * Math.PI * j / slices
                val sinLon = Math.sin(lon)
                val cosLon = Math.cos(lon)

                val x = (cosLon * cosLat).toFloat() * radius
                val y = sinLat.toFloat() * radius
                val z = (sinLon * cosLat).toFloat() * radius

                val u = j.toFloat() / slices
                val v = i.toFloat() / stacks

                vertices.add(x)
                vertices.add(y)
                vertices.add(z)
                vertices.add(u)
                vertices.add(v)
            }
        }

        for (i in 0 until stacks) {
            for (j in 0 until slices) {
                val first = (i * (slices + 1) + j).toShort()
                val second = ((i + 1) * (slices + 1) + j).toShort()

                indices.add(first)
                indices.add(second)
                indices.add((first + 1).toShort())

                indices.add(second)
                indices.add((second + 1).toShort())
                indices.add((first + 1).toShort())
            }
        }

        return SphereData(vertices.toFloatArray(), indices.toShortArray())
    }

    // Создание VBO для конкретной сферы
    fun createVBO(vertices: FloatArray): Int {
        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])

        val buffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(vertices); position(0) }

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, buffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        return vbo[0]
    }

    // Создание IBO для конкретной сферы
    fun createIBO(indices: ShortArray): Int {
        val ibo = IntArray(1)
        GLES20.glGenBuffers(1, ibo, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])

        val buffer: ShortBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply { put(indices); position(0) }

        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.size * 2, buffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        return ibo[0]
    }

    fun loadTexture(context: Context, resId: Int): Int {
        return TextureHelper.loadTexture(context, resId)
    }

    // Отрисовка отдельной сферы с индивидуальными VBO/IBO и текстурой
    fun drawSphere(
        vpMatrix: FloatArray,
        modelMatrix: FloatArray,
        textureId: Int,
        vbo: Int,
        ibo: Int,
        vertexCount: Int
    ) {
        GLES20.glUseProgram(programId)

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixLoc, 1, false, mvp, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(samplerLoc, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)

        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 5 * 4, 0)

        GLES20.glEnableVertexAttribArray(texLoc)
        GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, 5 * 4, 3 * 4)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexCount, GLES20.GL_UNSIGNED_SHORT, 0)

        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(texLoc)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun loadShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)
        }
    }
}
