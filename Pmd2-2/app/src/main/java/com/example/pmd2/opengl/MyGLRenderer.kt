package com.example.pmd2.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.pmd2.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var cube: Cube
    private lateinit var square: Square
    private var textureId: Int = 0

    // Матрицы
    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)

    // Угол вращения куба
    private var angle: Float = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Очистка экрана чёрным цветом и включение глубины
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Создание куба
        cube = Cube()

        // Загрузка текстуры галактики
        textureId = TextureHelper.loadTexture(context, R.drawable.galaxy)

        // Создание квадрата с текстурой
        square = Square(textureId)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Установка камеры
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 5f,   // Камера отодвинута назад
            0f, 0f, 0f,   // Смотрим в центр
            0f, 1f, 0f)   // Вверх по Y

        // Считаем общую матрицу VP
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        // Рисуем квадрат (фон) — растянут на весь экран
        square.draw(vpMatrix)

        // Вращение куба по центру
        angle += 1f
        cube.modelMatrix.identity()
        cube.modelMatrix.translate(0f, 0f, 0f)
        cube.modelMatrix.rotate(angle, 0.5f, 1f, 0f)
        cube.draw(vpMatrix)
    }

    // Вспомогательная функция для сброса матрицы модели куба
    private fun FloatArray.identity() {
        Matrix.setIdentityM(this, 0)
    }

    private fun FloatArray.translate(x: Float, y: Float, z: Float) {
        Matrix.translateM(this, 0, x, y, z)
    }

    private fun FloatArray.rotate(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.rotateM(this, 0, angle, x, y, z)
    }
}
