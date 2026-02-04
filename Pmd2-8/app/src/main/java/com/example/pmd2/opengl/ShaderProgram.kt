package com.example.pmd2.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object ShaderProgram {

    private var programId = 0
    private var posLoc = -1
    private var texLoc = -1
    private var mvpMatrixLoc = -1
    private var samplerLoc = -1

    // Для Phong
    private var phongProgramId = 0
    private var phongPosLoc = -1
    private var phongNormalLoc = -1
    private var phongTexLoc = -1
    private var phongMVPMatrixLoc = -1
    private var phongModelMatrixLoc = -1
    private var phongSamplerLoc = -1
    private var phongLightPosLoc = -1


    fun initStandardProgram() {
        if (programId != 0 && GLES20.glIsProgram(programId)) {
            Log.i("ShaderProgram", "Standard program already exists and is valid")
            return
        }

        Log.i("ShaderProgram", "Creating standard program now...")

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

        val vertexShader = loadAndCompileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode, "Standard VS")
        val fragmentShader = loadAndCompileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode, "Standard FS")

        programId = GLES20.glCreateProgram()
        if (programId == 0) {
            Log.e("ShaderProgram", "Failed to create standard program")
            return
        }

        GLES20.glAttachShader(programId, vertexShader)
        GLES20.glAttachShader(programId, fragmentShader)
        GLES20.glLinkProgram(programId)

        checkLinkStatus(programId, "Standard shader program")

        if (GLES20.glIsProgram(programId)) {
            posLoc = GLES20.glGetAttribLocation(programId, "vPosition")
            texLoc = GLES20.glGetAttribLocation(programId, "aTexCoord")
            mvpMatrixLoc = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
            samplerLoc = GLES20.glGetUniformLocation(programId, "uTexture")

            logLocationErrors("Standard", mapOf(
                "vPosition" to posLoc,
                "aTexCoord" to texLoc,
                "uMVPMatrix" to mvpMatrixLoc,
                "uTexture" to samplerLoc
            ))
        }

        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        checkGlError("After creating standard program")
    }

    fun initPhongShader() {
        if (phongProgramId != 0 && GLES20.glIsProgram(phongProgramId)) {
            Log.i("ShaderProgram", "Phong program already exists and is valid")
            return
        }

        Log.i("ShaderProgram", "Creating Phong program now...")

        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uModelMatrix;
            attribute vec4 vPosition;
            attribute vec3 aNormal;
            attribute vec2 aTexCoord;
            varying vec3 vNormal;
            varying vec3 vPositionEye;
            varying vec2 vTexCoord;
            void main() {
                vec3 normal = mat3(uModelMatrix) * aNormal;
                vNormal = normalize(normal);
                vec4 posEye = uModelMatrix * vPosition;
                vPositionEye = posEye.xyz / posEye.w;
                vTexCoord = aTexCoord;
                gl_Position = uMVPMatrix * vPosition;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            uniform vec3 uLightPos;
            varying vec3 vNormal;
            varying vec3 vPositionEye;
            varying vec2 vTexCoord;
            void main() {
                vec3 N = normalize(vNormal);
                vec3 L = normalize(uLightPos - vPositionEye);
                vec3 V = normalize(-vPositionEye);
                vec3 R = reflect(-L, N);
                
                float lambert = max(dot(N, L), 0.0);
                float spec = pow(max(dot(R, V), 0.0), 32.0);
                
                vec4 texColor = texture2D(uTexture, vTexCoord);
                vec3 color = texColor.rgb * (0.2 + 0.8 * lambert) + vec3(1.0) * spec * 0.5;
                
                gl_FragColor = vec4(color, texColor.a);
            }
        """.trimIndent()

        val vertexShader = loadAndCompileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode, "Phong VS")
        val fragmentShader = loadAndCompileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode, "Phong FS")

        phongProgramId = GLES20.glCreateProgram()
        if (phongProgramId == 0) {
            Log.e("ShaderProgram", "Failed to create Phong program")
            return
        }

        GLES20.glAttachShader(phongProgramId, vertexShader)
        GLES20.glAttachShader(phongProgramId, fragmentShader)
        GLES20.glLinkProgram(phongProgramId)

        checkLinkStatus(phongProgramId, "Phong shader program")

        if (GLES20.glIsProgram(phongProgramId)) {
            phongPosLoc = GLES20.glGetAttribLocation(phongProgramId, "vPosition")
            phongNormalLoc = GLES20.glGetAttribLocation(phongProgramId, "aNormal")
            phongTexLoc = GLES20.glGetAttribLocation(phongProgramId, "aTexCoord")
            phongMVPMatrixLoc = GLES20.glGetUniformLocation(phongProgramId, "uMVPMatrix")
            phongModelMatrixLoc = GLES20.glGetUniformLocation(phongProgramId, "uModelMatrix")
            phongSamplerLoc = GLES20.glGetUniformLocation(phongProgramId, "uTexture")
            phongLightPosLoc = GLES20.glGetUniformLocation(phongProgramId, "uLightPos")

            logLocationErrors("Phong", mapOf(
                "vPosition" to phongPosLoc,
                "aNormal" to phongNormalLoc,
                "aTexCoord" to phongTexLoc,
                "uMVPMatrix" to phongMVPMatrixLoc,
                "uModelMatrix" to phongModelMatrixLoc,
                "uTexture" to phongSamplerLoc,
                "uLightPos" to phongLightPosLoc
            ))
        }

        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        checkGlError("After creating Phong program")
    }

    private fun loadAndCompileShader(type: Int, code: String, name: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) {
            Log.e("ShaderProgram", "$name: Failed to create shader")
            return 0
        }

        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

        if (compileStatus[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            Log.e("ShaderProgram", "$name compile error:\n$log\nSource:\n$code")
            GLES20.glDeleteShader(shader)
            return 0
        }

        Log.i("ShaderProgram", "$name compiled successfully")
        return shader
    }

    private fun checkLinkStatus(program: Int, name: String) {
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] == 0) {
            val log = GLES20.glGetProgramInfoLog(program)
            Log.e("ShaderProgram", "$name link failed:\n$log")
            GLES20.glDeleteProgram(program)
            if (program == phongProgramId) phongProgramId = 0
            if (program == programId) programId = 0
        } else {
            Log.i("ShaderProgram", "$name linked successfully")
        }
        checkGlError("After linking $name")
    }

    private fun logLocationErrors(shaderName: String, locations: Map<String, Int>) {
        locations.forEach { (name, loc) ->
            if (loc < 0) {
                Log.e("ShaderProgram", "$shaderName: location not found -> $name")
            }
        }
    }

    private fun checkGlError(msg: String) {
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.e("GL_ERROR", "$msg: glError $error (0x${Integer.toHexString(error)})")
        }
    }

    data class SphereData(val vertices: FloatArray, val indices: ShortArray)

    fun createSphereData(radius: Float, stacks: Int, slices: Int, usePhong: Boolean = false): SphereData {
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

                if (usePhong) {
                    val length = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    if (length > 0f) {
                        vertices.add(x / length)
                        vertices.add(y / length)
                        vertices.add(z / length)
                    } else {
                        vertices.add(0f)
                        vertices.add(1f)
                        vertices.add(0f)
                    }
                }

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

    fun createVBO(vertices: FloatArray): Int {
        val vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        val buffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(vertices); position(0) }
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, buffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        checkGlError("createVBO")
        return vbo[0]
    }

    fun createIBO(indices: ShortArray): Int {
        val ibo = IntArray(1)
        GLES20.glGenBuffers(1, ibo, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0])
        val buffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply { put(indices); position(0) }
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.size * 2, buffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        checkGlError("createIBO")
        return ibo[0]
    }

    fun loadTexture(context: Context, resId: Int): Int {
        return TextureHelper.loadTexture(context, resId)
    }

    fun initStandardShader() {
        if (programId == 0 || !GLES20.glIsProgram(programId)) {
            Log.w("ShaderProgram", "Standard program invalid or not created → recreating")
            initStandardProgram()
        }
        if (programId == 0) {
            Log.e("ShaderProgram", "Cannot use standard shader — programId == 0")
            return
        }

        GLES20.glUseProgram(programId)
        checkGlError("glUseProgram standard")
    }

    fun drawSphere(
        vpMatrix: FloatArray,
        modelMatrix: FloatArray,
        textureId: Int,
        vbo: Int,
        ibo: Int,
        vertexCount: Int
    ) {
        if (programId == 0 || !GLES20.glIsProgram(programId)) {
            Log.e("drawSphere", "Standard program is invalid — recreating")
            initStandardProgram()
            if (programId == 0) return
        }

        initStandardShader()

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
        checkGlError("drawSphere after draw")

        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(texLoc)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun drawSpherePhong(
        vpMatrix: FloatArray,
        modelMatrix: FloatArray,
        textureId: Int,
        vbo: Int,
        ibo: Int,
        vertexCount: Int
    ) {
        if (phongProgramId == 0 || !GLES20.glIsProgram(phongProgramId)) {
            Log.e("drawSpherePhong", "Phong program is invalid — recreating")
            initPhongShader()
            if (phongProgramId == 0) return
        }

        GLES20.glUseProgram(phongProgramId)
        checkGlError("glUseProgram phong")

        val mvp = FloatArray(16)
        Matrix.multiplyMM(mvp, 0, vpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(phongMVPMatrixLoc, 1, false, mvp, 0)
        GLES20.glUniformMatrix4fv(phongModelMatrixLoc, 1, false, modelMatrix, 0)


        val lightWorldPos = floatArrayOf(5f, 5f, 5f, 1f)
        val invModelMatrix = FloatArray(16)
        val lightModelPos = FloatArray(4)

        if (Matrix.invertM(invModelMatrix, 0, modelMatrix, 0)) {
            Matrix.multiplyMV(lightModelPos, 0, invModelMatrix, 0, lightWorldPos, 0)
            GLES20.glUniform3f(phongLightPosLoc,
                lightModelPos[0] / lightModelPos[3],
                lightModelPos[1] / lightModelPos[3],
                lightModelPos[2] / lightModelPos[3]
            )
        } else {
            Log.w("ShaderProgram", "Cannot invert model matrix, using fallback light position")
            GLES20.glUniform3f(phongLightPosLoc, 0f, 0f, 5f)
        }


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(phongSamplerLoc, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)

        val stride = 8 * 4
        GLES20.glEnableVertexAttribArray(phongPosLoc)
        GLES20.glVertexAttribPointer(phongPosLoc, 3, GLES20.GL_FLOAT, false, stride, 0)

        GLES20.glEnableVertexAttribArray(phongNormalLoc)
        GLES20.glVertexAttribPointer(phongNormalLoc, 3, GLES20.GL_FLOAT, false, stride, 3 * 4)

        GLES20.glEnableVertexAttribArray(phongTexLoc)
        GLES20.glVertexAttribPointer(phongTexLoc, 2, GLES20.GL_FLOAT, false, stride, 6 * 4)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vertexCount, GLES20.GL_UNSIGNED_SHORT, 0)
        checkGlError("drawSpherePhong after draw")

        GLES20.glDisableVertexAttribArray(phongPosLoc)
        GLES20.glDisableVertexAttribArray(phongNormalLoc)
        GLES20.glDisableVertexAttribArray(phongTexLoc)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }
}