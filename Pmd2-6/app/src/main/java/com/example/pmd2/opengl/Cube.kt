package com.example.pmd2.opengl

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Cube(private val size: Float = 1f) {

    val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

    private val vertices: FloatBuffer
    private val indices: ShortBuffer
    private val vertexCount: Int
    private val vbo: Int
    private val ibo: Int

    private var programId = 0
    private var posLoc = 0
    private var mvpLoc = 0
    private var colorLoc = 0

    init {
        // Куб со стороной size, центр в начале координат
        val s = size / 2f
        val verts = floatArrayOf(
            -s, -s, -s,
            s, -s, -s,
            s,  s, -s,
            -s,  s, -s,
            -s, -s,  s,
            s, -s,  s,
            s,  s,  s,
            -s,  s,  s
        )

        val inds = shortArrayOf(
            0,1,2, 0,2,3, // back
            4,5,6, 4,6,7, // front
            0,4,7, 0,7,3, // left
            1,5,6, 1,6,2, // right
            3,2,6, 3,6,7, // top
            0,1,5, 0,5,4  // bottom
        )

        vertexCount = inds.size

        // Буферы
        vertices = ByteBuffer.allocateDirect(verts.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(verts); position(0) }

        indices = ByteBuffer.allocateDirect(inds.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply { put(inds); position(0) }

        // Создаем VBO и IBO
        val buffers = IntArray(2)
        GLES20.glGenBuffers(2, buffers, 0)
        vbo = buffers[0]
        ibo = buffers[1]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.capacity() * 4, vertices, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * 2, indices, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        // Шейдер для куба с прозрачностью
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec3 aPosition;
            void main() {
                gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 uColor;
            void main() {
                gl_FragColor = uColor;
            }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        posLoc = GLES20.glGetAttribLocation(programId, "aPosition")
        mvpLoc = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        colorLoc = GLES20.glGetUniformLocation(programId, "uColor")
    }

    /**
     * Рисует куб.
     * @param vpMatrix матрица вида и проекции
     * @param modelMatrix матрица модели
     * @param color массив из 4 элементов RGBA (0..1), прозрачность включена
     */
    fun draw(vpMatrix: FloatArray, modelMatrix: FloatArray, color: FloatArray = floatArrayOf(1f,1f,1f,0.3f)) {
        GLES20.glUseProgram(programId)

        // Включаем прозрачность
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glDepthMask(false) // чтобы куб не затирал планету

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvp, 0)

        GLES20.glUniform4f(colorLoc, color[0], color[1], color[2], color[3])

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)

        GLES20.glEnableVertexAttribArray(posLoc)
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 3*4, 0)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexCount, GLES20.GL_UNSIGNED_SHORT, 0)

        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        GLES20.glDepthMask(true)
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)
        }
    }
}
