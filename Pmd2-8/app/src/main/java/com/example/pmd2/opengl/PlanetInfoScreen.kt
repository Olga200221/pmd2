package com.example.pmd2.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pmd2.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Composable
fun PlanetInfoScreen(
    selectedIndex: Int,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val isLuna = selectedIndex == 9
    val isNeptune = selectedIndex == 8  // ← исправлено: Нептун = 8

    val title = when {
        isLuna -> "Луна"
        isNeptune -> "Нептун"
        else -> "Планета"
    }

    val description = when {
        isLuna -> "Луна — единственный естественный спутник Земли. Диаметр около 3474 км (примерно 1/4 Земли). " +
                "Поверхность покрыта кратерами, нет атмосферы, поэтому днём температура до +127 °C, ночью до -173 °C. " +
                "Всегда повёрнута к Земле одной стороной (синхронное вращение)."
        isNeptune -> "Нептун — восьмая и самая дальняя планета Солнечной системы. Ледяной гигант с самыми сильными ветрами в Солнечной системе (до 2100 км/ч). " +
                "Атмосфера состоит в основном из водорода, гелия и метана, который придаёт планете характерный глубокий синий цвет. " +
                "Здесь показана процедурная симуляция его динамичной атмосферы с движущимися переливающимися волнами."
        else -> "Описание отсутствует"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3D1E3F),
                    contentColor = Color.White
                )
            ) {
                Text("Назад")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .background(Color.Black)
        ) {
            AndroidView(factory = {
                val glView = GLSurfaceView(it)
                glView.setEGLContextClientVersion(2)
                glView.setRenderer(PlanetRenderer(context, selectedIndex))
                glView
            })
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFFD1D3D0),
                    fontSize = 32.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFB78FB1),
                    fontSize = 18.sp,
                    lineHeight = 28.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

class PlanetRenderer(
    private val context: Context,
    private val index: Int
) : GLSurfaceView.Renderer {

    private lateinit var sphere: Sphere
    private val viewMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private var angle = 0f
    private var time = 0f  // для анимации волн на Нептуне

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        ShaderProgram.initStandardProgram()
        ShaderProgram.initPhongShader()

        // Инициализируем процедурный шейдер только для Нептуна
        if (index == 8) {
            ShaderProgram.initNeptuneShader()
        }

        // Выбор текстуры и типа шейдера
        val textureRes: Int
        val usePhong = true
        val isProcedural = (index == 8)

        textureRes = when (index) {
            9 -> R.drawable.moon      // Луна
            8 -> 0                    // Нептун — procedural, без текстуры
            else -> R.drawable.earth  // остальные планеты по умолчанию
        }

        sphere = Sphere(context, textureRes, 1.5f, usePhong = usePhong, isProcedural = isProcedural)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 100f)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        time += 0.016f  // ≈60 fps

        angle += 0.5f
        sphere.modelMatrix.identity()
        sphere.modelMatrix.rotate(angle, 0f, 1f, 0f)

        if (index == 8 && sphere.isProcedural) {
            // Рисуем Нептун с процедурными волнами
            ShaderProgram.drawSphereNeptune(
                vpMatrix,
                sphere.modelMatrix,
                sphere.vbo,
                sphere.ibo,
                sphere.vertexCount,
                time
            )
        } else {
            sphere.draw(vpMatrix)
        }
    }

    private fun FloatArray.identity() = Matrix.setIdentityM(this, 0)
    private fun FloatArray.rotate(angle: Float, x: Float, y: Float, z: Float) =
        Matrix.rotateM(this, 0, angle, x, y, z)
}