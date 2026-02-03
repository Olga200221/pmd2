package com.example.pmd2.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pmd2.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Composable
fun PlanetInfoScreen(selectedIndex: Int) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = {
            val glView = GLSurfaceView(it)
            glView.setEGLContextClientVersion(2)
            // Для Луны (индекс 9) используем Phong освещение
            glView.setRenderer(PlanetRenderer(context, selectedIndex, usePhong = selectedIndex == 9))
            glView
        })
    }
}

// Рендерер для выбранной планеты/спутника
class PlanetRenderer(
    private val context: Context,
    private val index: Int,
    private val usePhong: Boolean = false
) : GLSurfaceView.Renderer {

    private lateinit var sphere: Sphere
    private val viewMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private var angle = 0f

    // Список текстур для планет + Луна (индекс соответствует GalaxyScreen)
    private val textures = listOf(
        R.drawable.mercury, R.drawable.venus, R.drawable.earth, R.drawable.mars,
        R.drawable.jupiter, R.drawable.saturn, R.drawable.uranus, R.drawable.neptune,
        R.drawable.moon // 8-й индекс — Луна
    )

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Инициализация шейдеров после создания контекста
        ShaderProgram.initStandardProgram()
        if (usePhong) {
            ShaderProgram.initPhongShader()
        }

        // Улучшенный выбор текстуры
        val textureRes = when {
            usePhong -> R.drawable.moon                      // если Phong → точно Луна
            index in textures.indices -> textures[index]     // безопасно берём из списка
            else -> R.drawable.earth                         // fallback только если совсем неверный индекс
        }

        // Для отладки — посмотри в Logcat
        android.util.Log.d("PlanetRenderer",
            "index = $index, usePhong = $usePhong, texture = $textureRes"
        )

        sphere = Sphere(context, textureRes, 1f, usePhong = usePhong)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Камера
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 3f,   // камера
            0f, 0f, 0f,   // центр
            0f, 1f, 0f
        )
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        // Вращение планеты/спутника
        angle += 0.5f
        sphere.modelMatrix.identity()
        sphere.modelMatrix.rotate(angle, 0f, 1f, 0f)

        sphere.draw(vpMatrix)
    }

    // Расширения для работы с матрицами
    private fun FloatArray.identity() {
        Matrix.setIdentityM(this, 0)
    }

    private fun FloatArray.rotate(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.rotateM(this, 0, angle, x, y, z)
    }
}