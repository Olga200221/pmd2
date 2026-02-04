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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pmd2.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import androidx.compose.ui.unit.sp

@Composable
fun PlanetInfoScreen(
    selectedIndex: Int,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val isLuna = selectedIndex == 9
    val isNeptuneWater = selectedIndex == 8

    val title = when {
        isLuna -> "Луна"
        isNeptuneWater -> "Нептун"
        else -> "Неизвестный объект"
    }

    val description = when {
        isLuna -> "Луна — единственный естественный спутник Земли. Диаметр около 3474 км (примерно 1/4 Земли). " +
                "Поверхность покрыта кратерами, нет атмосферы, поэтому днём температура до +127 °C, ночью до -173 °C. " +
                "Всегда повёрнута к Земле одной стороной (синхронное вращение)."
        isNeptuneWater -> "Нептун — восьмая и самая дальняя планета. Ледяной гигант с самыми сильными ветрами (до 2100 км/ч). " +
                "Здесь показана фантастическая интерпретация его поверхности как бесконечная водная гладь с волнами."
        else -> "Описание отсутствует"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)  // ← чёрный фон всего экрана
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
                    containerColor = Color(0xFF3D1E3F),  // тёмно-фиолетовый из палитры
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


                val usePhong = isLuna
                glView.setRenderer(PlanetRenderer(context, selectedIndex, usePhong))

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
                    color = Color(0xFFD1D3D0),  // светло-серый заголовок
                    fontSize = 32.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFB78FB1),  // розово-лиловый текст
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
    private val index: Int,
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


        val textureRes = when (index) {
            9 -> R.drawable.moon
            8 -> R.drawable.water
            else -> R.drawable.earth
        }

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