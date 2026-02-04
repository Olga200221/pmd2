package com.example.pmd2.opengl

import android.content.Context
import android.opengl.Matrix

class Sphere(
    context: Context,
    textureRes: Int,
    val radius: Float,
    private val usePhong: Boolean = false
) {

    var modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
    private val textureId: Int


    private val vbo: Int
    private val ibo: Int
    private val vertexCount: Int

    init {
        textureId = ShaderProgram.loadTexture(context, textureRes)

        val sphereData = ShaderProgram.createSphereData(1.0f, 48, 48, usePhong)

        vbo = ShaderProgram.createVBO(sphereData.vertices)
        ibo = ShaderProgram.createIBO(sphereData.indices)
        vertexCount = sphereData.indices.size

        if (usePhong) {
            ShaderProgram.initPhongShader()
        } else {
            ShaderProgram.initStandardShader()
        }
    }

    fun draw(vpMatrix: FloatArray) {
        val scaledMatrix = FloatArray(16)
        Matrix.setIdentityM(scaledMatrix, 0)
        Matrix.scaleM(scaledMatrix, 0, radius, radius, radius)
        Matrix.multiplyMM(scaledMatrix, 0, modelMatrix, 0, scaledMatrix, 0)

        if (usePhong) {
            ShaderProgram.drawSpherePhong(vpMatrix, scaledMatrix, textureId, vbo, ibo, vertexCount)
        } else {
            ShaderProgram.drawSphere(vpMatrix, scaledMatrix, textureId, vbo, ibo, vertexCount)
        }
    }
}