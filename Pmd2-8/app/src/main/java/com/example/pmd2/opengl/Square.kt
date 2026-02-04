package com.example.pmd2.opengl

import android.opengl.GLES20
import android.opengl.Matrix

class Square(private val textureId: Int) {

    private val vertices = floatArrayOf(
        -1f,  1f, 0f,
        -1f, -1f, 0f,
        1f, -1f, 0f,
        1f,  1f, 0f
    )

    private val texCoords = floatArrayOf(
        0f, 0f,
        0f, 1f,
        1f, 1f,
        1f, 0f
    )

    private val indices = shortArrayOf(0, 1, 2, 0, 2, 3)

    var modelMatrix = FloatArray(16)
    var mvpMatrix = FloatArray(16)
    private var program = 0

    init {
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
                vec4 texColor = texture2D(uTexture, vTexCoord);
                if (texColor.a < 0.05) {  // отсекаем почти полностью прозрачные пиксели (фон)
                    discard;
                }
                gl_FragColor = texColor;
            }
        """.trimIndent()

        val vertexShader = TextureHelper.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = TextureHelper.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val log = GLES20.glGetProgramInfoLog(program)
            android.util.Log.e("Square", "Link error: $log")
        }

        val vertCompileStatus = IntArray(1)
        GLES20.glGetShaderiv(vertexShader, GLES20.GL_COMPILE_STATUS, vertCompileStatus, 0)
        if (vertCompileStatus[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(vertexShader)
            android.util.Log.e("Square", "Vertex shader compile error: $log")
        }

        val fragCompileStatus = IntArray(1)
        GLES20.glGetShaderiv(fragmentShader, GLES20.GL_COMPILE_STATUS, fragCompileStatus, 0)
        if (fragCompileStatus[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(fragmentShader)
            android.util.Log.e("Square", "Fragment shader compile error: $log")
        }

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -1f)
    }

    fun draw(vpMatrix: FloatArray) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        GLES20.glUseProgram(program)

        val mvpLoc = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val posLoc = GLES20.glGetAttribLocation(program, "vPosition")
        val texLoc = GLES20.glGetAttribLocation(program, "aTexCoord")
        val samplerLoc = GLES20.glGetUniformLocation(program, "uTexture")

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvpMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(samplerLoc, 0)

        TextureHelper.bindVertices(vertices, posLoc)
        TextureHelper.bindTextureCoords(texCoords, texLoc)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            TextureHelper.createIndexBuffer(indices)
        )

        TextureHelper.unbind(posLoc, texLoc)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }
}