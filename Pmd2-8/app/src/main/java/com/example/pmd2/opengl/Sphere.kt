package com.example.pmd2.opengl

import android.content.Context
import android.opengl.Matrix

class Sphere(
    context: Context,
    textureRes: Int,
    val radius: Float,
    private val usePhong: Boolean = false,
    val isProcedural: Boolean = false  // ← добавлен параметр
) {

    var modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
    private val textureId: Int

    // Делаем поля доступными для MyGLRenderer (internal = доступ в модуле)
    internal val vbo: Int
    internal val ibo: Int
    internal val vertexCount: Int

    init {
        // Для procedural (Нептун) текстуру не загружаем
        textureId = if (isProcedural) 0 else ShaderProgram.loadTexture(context, textureRes)

        // Генерируем данные сферы (с нормалями, если Phong или procedural)
        val sphereData = ShaderProgram.createSphereData(
            1.0f,
            48,
            48,
            usePhong || isProcedural
        )

        vbo = ShaderProgram.createVBO(sphereData.vertices)
        ibo = ShaderProgram.createIBO(sphereData.indices)
        vertexCount = sphereData.indices.size

        // Инициализируем нужный шейдер
        if (isProcedural || usePhong) {
            ShaderProgram.initPhongShader()     // procedural тоже использует нормали
        } else {
            ShaderProgram.initStandardShader()
        }
    }

    fun draw(vpMatrix: FloatArray) {
        val scaleMatrix = FloatArray(16).apply {
            Matrix.setIdentityM(this, 0)
            Matrix.scaleM(this, 0, radius, radius, radius)
        }

        val finalMatrix = FloatArray(16)
        // Правильный порядок: modelMatrix × scale (масштаб применяется в локальной системе)
        Matrix.multiplyMM(finalMatrix, 0, modelMatrix, 0, scaleMatrix, 0)

        if (isProcedural) {
            // Процедурный рендеринг Нептуна (время передаётся отдельно из рендерера)
            // Здесь мы не передаём time, т.к. draw() вызывается для обычных сфер
            // Для Нептуна в MyGLRenderer будет отдельный вызов drawSphereNeptune
            // Поэтому здесь можно оставить заглушку или бросить исключение
            throw IllegalStateException("Procedural spheres must be drawn via drawSphereNeptune")
        } else if (usePhong) {
            ShaderProgram.drawSpherePhong(vpMatrix, finalMatrix, textureId, vbo, ibo, vertexCount)
        } else {
            ShaderProgram.drawSphere(vpMatrix, finalMatrix, textureId, vbo, ibo, vertexCount)
        }
    }
}