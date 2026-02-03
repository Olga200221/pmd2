package com.example.pmd2.opengl

import android.opengl.GLES20
import android.opengl.Matrix

class Cube {

    private val vertices = floatArrayOf(
        -1f,  1f,  1f,  // 0
        -1f, -1f,  1f,  // 1
        1f, -1f,  1f,  // 2
        1f,  1f,  1f,  // 3
        -1f,  1f, -1f,  // 4
        -1f, -1f, -1f,  // 5
        1f, -1f, -1f,  // 6
        1f,  1f, -1f   // 7
    )

    private val indices = shortArrayOf(
        0,1,2, 0,2,3, // Front
        4,5,6, 4,6,7, // Back
        4,5,1, 4,1,0, // Left
        3,2,6, 3,6,7, // Right
        4,0,3, 4,3,7, // Top
        1,5,6, 1,6,2  // Bottom
    )

    private val color = floatArrayOf(0.8f, 0.2f, 0.2f, 1f)

    val modelMatrix = FloatArray(16)
    val mvpMatrix = FloatArray(16)
    private var program = 0

    init {
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()

        val vertexShader = TextureHelper.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = TextureHelper.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        Matrix.setIdentityM(modelMatrix, 0)
    }

    fun setRotation(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angle, x, y, z)
    }

    fun draw(vpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val mvpLoc = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val posLoc = GLES20.glGetAttribLocation(program, "vPosition")
        val colorLoc = GLES20.glGetUniformLocation(program, "vColor")

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniform4fv(colorLoc, 1, color, 0)

        TextureHelper.bindVertices(vertices, posLoc)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            TextureHelper.createIndexBuffer(indices)
        )
        TextureHelper.unbind(posLoc)
    }
}
