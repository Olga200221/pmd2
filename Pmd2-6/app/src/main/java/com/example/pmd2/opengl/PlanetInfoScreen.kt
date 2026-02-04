package com.example.pmd2.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pmd2.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Composable
fun PlanetInfoScreen(
    selectedIndex: Int,
    onBackClick: () -> Unit = {}  // ← добавлен параметр для возврата назад
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Кнопка "Назад" вверху экрана
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = onBackClick) {
                Text("Назад")
            }
        }

        // 3D-модель Луны занимает основное пространство
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(factory = {
                val glView = GLSurfaceView(it)
                glView.setEGLContextClientVersion(2)
                glView.setRenderer(PlanetRenderer(context, selectedIndex, usePhong = true)) // всегда Phong для Луны
                glView
            })
        }

        // Описание Луны внизу
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Луна",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Луна — единственный естественный спутник Земли. Диаметр около 3474 км (примерно 1/4 Земли). " +
                        "Поверхность покрыта кратерами, нет атмосферы, поэтому днём температура до +127 °C, ночью до -173 °C. " +
                        "Всегда повёрнута к Земле одной стороной (синхронное вращение).",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Рендерер остаётся почти без изменений, только убираем ненужную логику индекса
class PlanetRenderer(
    private val context: Context,
    private val index: Int,          // можно оставить, но не используется
    private val usePhong: Boolean
) : GLSurfaceView.Renderer {

    private lateinit var sphere: Sphere
    private val viewMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private var angle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        ShaderProgram.initStandardProgram()
        if (usePhong) {
            ShaderProgram.initPhongShader()
        }

        // Для этого экрана всегда Луна
        val textureRes = R.drawable.moon

        sphere = Sphere(context, textureRes, 1f, usePhong = usePhong)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        angle += 0.5f
        sphere.modelMatrix.identity()
        sphere.modelMatrix.rotate(angle, 0f, 1f, 0f)

        sphere.draw(vpMatrix)
    }

    private fun FloatArray.identity() = Matrix.setIdentityM(this, 0)
    private fun FloatArray.rotate(angle: Float, x: Float, y: Float, z: Float) =
        Matrix.rotateM(this, 0, angle, x, y, z)
}