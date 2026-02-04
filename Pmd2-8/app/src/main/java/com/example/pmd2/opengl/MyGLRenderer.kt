package com.example.pmd2.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.pmd2.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random
import kotlin.math.abs

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var square: Square
    private var galaxyTexture: Int = 0

    private lateinit var sun: Sphere
    private val planets = mutableListOf<Sphere>()
    private val planetDistances = floatArrayOf(0.95f, 1.4f, 2.0f, 2.5f, 3.5f, 4.5f, 5.2f, 5.8f)
    private val planetSizes = floatArrayOf(
        0.2f, 0.35f, 0.5f, 0.3f, 1.0f, 0.85f, 0.6f, 0.5f
    )
    private val planetTextures = intArrayOf(
        R.drawable.mercury, R.drawable.venus, R.drawable.earth, R.drawable.mars,
        R.drawable.jupiter, R.drawable.saturn, R.drawable.uranus, R.drawable.neptune
    )
    private var moon: Sphere? = null
    private var moonDistance = 0.7f

    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private val orthoMatrix = FloatArray(16)

    private var angleSun = 0f
    private val planetAngles = FloatArray(8) { 0f }
    private var moonAngle = 0f

    private val orbitTilt = 30f

    var selectedPlanetIndex: Int = 0

    private lateinit var selectionCube: Cube

    private lateinit var blackHoleDisk: Square
    private var blackHoleX = 0f
    private var blackHoleY = 0f
    private var blackHoleZ = -5f
    private var blackHoleVelX = 0.015f
    private var blackHoleVelY = 0.01f
    private val blackHoleSize = 8.0f
    private val blackHoleAlpha = 0.8f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)           // ← включаем смешивание для прозрачности
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        galaxyTexture = TextureHelper.loadTexture(context, R.drawable.galaxy)
        square = Square(galaxyTexture)

        sun = Sphere(context, R.drawable.sun, 0.6f)

        for (i in 0..7) planets.add(Sphere(context, planetTextures[i], planetSizes[i]))

        moon = Sphere(context, R.drawable.moon, 0.1f)

        selectionCube = Cube(1f)

        val bhTex = TextureHelper.loadTexture(context, R.drawable.black_hole)
        android.util.Log.d("MyGLRenderer", "black_hole texture ID: $bhTex")  // ← лог, чтобы увидеть, загрузилась ли текстура
        blackHoleDisk = Square(bhTex)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 100f)
        Matrix.orthoM(orthoMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        square.draw(orthoMatrix)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 3f, 8f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        blackHoleX += blackHoleVelX
        blackHoleY += blackHoleVelY

        if (abs(blackHoleX) > 8f) blackHoleVelX = -blackHoleVelX
        if (abs(blackHoleY) > 5f) blackHoleVelY = -blackHoleVelY

        if (Random.nextFloat() < 0.005f) {
            blackHoleVelX += Random.nextFloat() * 0.01f - 0.005f
            blackHoleVelY += Random.nextFloat() * 0.01f - 0.005f
        }

        val bhModel = FloatArray(16)
        Matrix.setIdentityM(bhModel, 0)
        Matrix.translateM(bhModel, 0, blackHoleX, blackHoleY, blackHoleZ)
        Matrix.rotateM(bhModel, 0, 60f, 1f, 0f, 0f)
        Matrix.rotateM(bhModel, 0, angleSun * 0.5f, 0f, 0f, 1f)

        blackHoleDisk.modelMatrix = bhModel

        blackHoleDisk.draw(vpMatrix)

        angleSun += 0.2f
        sun.modelMatrix.identity()
        sun.modelMatrix.rotate(angleSun, 0f, 1f, 0f)
        sun.draw(vpMatrix)

        if (selectedPlanetIndex == 0) {
            drawSelectionCubeForSun()
        }

        for (i in planets.indices) {
            planetAngles[i] += (i + 1) * 0.3f
            planets[i].modelMatrix.identity()
            planets[i].modelMatrix.rotate(orbitTilt, 1f, 0f, 0f)
            planets[i].modelMatrix.rotate(planetAngles[i], 0f, 1f, 0f)
            planets[i].modelMatrix.translate(planetDistances[i], 0f, 0f)
            planets[i].draw(vpMatrix)

            if (selectedPlanetIndex == i + 1) {
                drawSelectionCube(i)
            }
        }

        moon?.let {
            it.modelMatrix.identity()
            it.modelMatrix.rotate(orbitTilt, 1f, 0f, 0f)
            it.modelMatrix.rotate(planetAngles[2], 0f, 1f, 0f)
            it.modelMatrix.translate(planetDistances[2], 0f, 0f)
            it.modelMatrix.rotate(moonAngle, 0f, 1f, 0f)
            it.modelMatrix.translate(moonDistance, 0f, 0f)
            it.draw(vpMatrix)

            if (selectedPlanetIndex == 9) {
                drawSelectionCubeForMoon()
            }
        }
    }

    private fun drawSelectionCube(planetIndex: Int) {
        val size = planetSizes[planetIndex]
        val distance = planetDistances[planetIndex]
        val scale = size * 1.8f

        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)
        model.rotate(orbitTilt, 1f, 0f, 0f)
        model.rotate(planetAngles[planetIndex], 0f, 1f, 0f)
        model.translate(distance, 0f, 0f)
        model.scale(scale, scale, scale)
        selectionCube.draw(vpMatrix, model, floatArrayOf(0f, 1f, 1f, 0.3f))
    }

    private fun drawSelectionCubeForSun() {
        val scale = 0.5f * 1.8f
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)
        model.rotate(angleSun, 0f, 1f, 0f)
        model.scale(scale, scale, scale)
        selectionCube.draw(vpMatrix, model, floatArrayOf(1f, 1f, 0f, 0.3f))
    }

    private fun drawSelectionCubeForMoon() {
        val scale = 0.1f * 1.8f
        val model = FloatArray(16)
        Matrix.setIdentityM(model, 0)
        model.rotate(orbitTilt, 1f, 0f, 0f)
        model.rotate(planetAngles[2], 0f, 1f, 0f)
        model.translate(planetDistances[2], 0f, 0f)
        model.rotate(moonAngle, 0f, 1f, 0f)
        model.translate(moonDistance, 0f, 0f)
        model.scale(scale, scale, scale)
        selectionCube.draw(vpMatrix, model, floatArrayOf(0f, 1f, 1f, 0.3f))
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

    private fun FloatArray.scale(x: Float, y: Float, z: Float) {
        Matrix.scaleM(this, 0, x, y, z)
    }
}