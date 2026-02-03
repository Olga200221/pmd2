package com.example.pmd2.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.pmd2.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var square: Square
    private var galaxyTexture: Int = 0

    // Солнце и планеты
    private lateinit var sun: Sphere
    private val planets = mutableListOf<Sphere>()
    private val planetDistances = floatArrayOf(0.8f, 1.4f, 2.0f, 2.5f, 3.5f, 4.5f, 5.2f, 5.8f)
    private val planetSizes = floatArrayOf(
        0.2f,  // Mercury
        0.35f, // Venus
        0.5f,  // Earth
        0.3f,  // Mars
        1.2f,  // Jupiter
        1.0f,  // Saturn
        0.7f,  // Uranus
        0.65f  // Neptune
    )
    private val planetTextures = intArrayOf(
        R.drawable.mercury, R.drawable.venus, R.drawable.earth, R.drawable.mars,
        R.drawable.jupiter, R.drawable.saturn, R.drawable.uranus, R.drawable.neptune
    )
    private var moon: Sphere? = null
    private var moonDistance = 0.7f // Луна от Земли

    // Матрицы
    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private val orthoMatrix = FloatArray(16)

    // Вращение
    private var angleSun = 0f
    private val planetAngles = FloatArray(8) { 0f }
    private var moonAngle = 0f

    // Наклон плоскости орбит (диагональная)
    private val orbitTilt = 30f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Фон
        galaxyTexture = TextureHelper.loadTexture(context, R.drawable.galaxy)
        square = Square(galaxyTexture)

        // Солнце
        sun = Sphere(context, R.drawable.sun, 0.5f)

        // Планеты
        for (i in 0..7) {
            planets.add(Sphere(context, planetTextures[i], planetSizes[i]))
        }

        // Луна для Земли
        moon = Sphere(context, R.drawable.moon, 0.1f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 100f)

        // Ортографическая проекция для фона
        Matrix.orthoM(orthoMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Фон
        square.draw(orthoMatrix)

        // Камера
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 3f, 8f,
            0f, 0f, 0f,
            0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        // Вращение Солнца
        angleSun += 0.2f
        sun.modelMatrix.identity()
        sun.modelMatrix.rotate(angleSun, 0f, 1f, 0f)
        sun.draw(vpMatrix)

        // Вращение планет по наклоненной плоскости
        for (i in planets.indices) {
            planetAngles[i] += (i + 1) * 0.5f
            planets[i].modelMatrix.identity()
            planets[i].modelMatrix.rotate(orbitTilt, 1f, 0f, 0f)          // наклон плоскости орбиты
            planets[i].modelMatrix.rotate(planetAngles[i], 0f, 1f, 0f)    // вращение планеты
            planets[i].modelMatrix.translate(planetDistances[i], 0f, 0f)
            planets[i].draw(vpMatrix)
        }

        // Луна вращается вокруг Земли по собственной наклонной орбите
        moon?.let {
            it.modelMatrix.identity()
            // Сначала наклон орбиты всей системы
            it.modelMatrix.rotate(orbitTilt, 1f, 0f, 0f)
            // Перемещение к позиции Земли
            it.modelMatrix.rotate(planetAngles[2], 0f, 1f, 0f)
            it.modelMatrix.translate(planetDistances[2], 0f, 0f)
            // Луна вращается вокруг Земли по оси Y
            it.modelMatrix.rotate(moonAngle, 0f, 1f, 0f)
            it.modelMatrix.translate(moonDistance, 0f, 0f)
            it.draw(vpMatrix)
        }



    }

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
