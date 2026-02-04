package com.example.pmd2.opengl

import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun GalaxyScreen(
    onDetailClick: (selectedIndex: Int) -> Unit = {},   // для статичных планет и Солнца
    onLunaClick: (selectedIndex: Int) -> Unit = {}      // для Луны (3D с Phong)
) {
    val context = LocalContext.current

    // State для выбранного объекта: 0 = Солнце, 1..8 = планеты, 9 = Луна
    var selectedPlanetIndex by remember { mutableStateOf(0) }

    // Ссылка на renderer для обновления выбранного объекта
    var rendererRef by remember { mutableStateOf<MyGLRenderer?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // GLSurfaceView (основная сцена с Солнцем и планетами)
        AndroidView(factory = { ctx ->
            MyGLSurfaceView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                val renderer = this.renderer as? MyGLRenderer
                rendererRef = renderer
                renderer?.selectedPlanetIndex = selectedPlanetIndex
            }
        }, modifier = Modifier.fillMaxSize())

        // Кнопки управления внизу
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Влево
            Button(onClick = {
                selectedPlanetIndex = (selectedPlanetIndex - 1 + 10) % 10
                rendererRef?.selectedPlanetIndex = selectedPlanetIndex
            }) {
                Text("←")
            }

            // Кнопка "Информация"
            Button(onClick = {
                if (selectedPlanetIndex == 9) {
                    // Луна → открываем экран с 3D-моделью + описанием
                    onLunaClick(9)
                } else {
                    // Солнце и планеты → открываем экран со статичной картинкой + описанием
                    onDetailClick(selectedPlanetIndex)
                }
            }) {
                Text("i")
            }

            // Вправо
            Button(onClick = {
                selectedPlanetIndex = (selectedPlanetIndex + 1) % 10
                rendererRef?.selectedPlanetIndex = selectedPlanetIndex
            }) {
                Text("→")
            }
        }
    }
}