package com.example.pmd2.opengl

import android.content.Context
import android.opengl.Matrix

class Sphere(context: Context, textureRes: Int, val radius: Float) {

    val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
    private val textureId: Int

    // Индивидуальные буферы для этой сферы
    private val vbo: Int
    private val ibo: Int
    private val vertexCount: Int

    init {
        // Загружаем текстуру
        textureId = ShaderProgram.loadTexture(context, textureRes)

        // Генерируем данные сферы (позиции, текстурные координаты и индексы)
        val sphereData = ShaderProgram.createSphereData(1.0f, 48, 48)

        // Создаем отдельные VBO/IBO для этой сферы
        vbo = ShaderProgram.createVBO(sphereData.vertices)
        ibo = ShaderProgram.createIBO(sphereData.indices)
        vertexCount = sphereData.indices.size
    }

    fun draw(vpMatrix: FloatArray) {
        // Создаем матрицу с масштабированием радиуса сферы
        val scaledMatrix = FloatArray(16)
        Matrix.setIdentityM(scaledMatrix, 0)
        Matrix.scaleM(scaledMatrix, 0, radius, radius, radius)
        Matrix.multiplyMM(scaledMatrix, 0, modelMatrix, 0, scaledMatrix, 0)

        // Отрисовка сферы с уникальной текстурой и буферами
        ShaderProgram.drawSphere(vpMatrix, scaledMatrix, textureId, vbo, ibo, vertexCount)
    }
}
