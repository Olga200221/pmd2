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
    onInfoClick: (selectedIndex: Int) -> Unit = {}
) {
    val context = LocalContext.current

    // State для выбранного объекта: 0 = Солнце, 1..8 = планеты, 9 = Луна
    var selectedPlanetIndex by remember { mutableStateOf(0) }

    // Ссылка на renderer для обновления выбранного объекта
    var rendererRef by remember { mutableStateOf<MyGLRenderer?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // GLSurfaceView
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

        // Кнопки внизу экрана
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Влево
            Button(onClick = {
                selectedPlanetIndex =
                    (selectedPlanetIndex - 1 + 10) % 10 // теперь 0..9
                rendererRef?.selectedPlanetIndex = selectedPlanetIndex
            }) {
                Text("←")
            }

            // Информация
            Button(onClick = { onInfoClick(selectedPlanetIndex) }) {
                Text("i")
            }

            // Вправо
            Button(onClick = {
                selectedPlanetIndex =
                    (selectedPlanetIndex + 1) % 10 // теперь 0..9
                rendererRef?.selectedPlanetIndex = selectedPlanetIndex
            }) {
                Text("→")
            }
        }
    }
}
