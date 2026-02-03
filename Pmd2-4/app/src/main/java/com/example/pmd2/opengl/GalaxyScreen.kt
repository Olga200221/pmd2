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
fun GalaxyScreen() {
    val context = LocalContext.current

    // State для выбранной планеты
    var selectedPlanetIndex by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        // GLSurfaceView
        AndroidView(factory = { ctx ->
            MyGLSurfaceView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                // Передаем selectedPlanetIndex в renderer
                (this.renderer as? MyGLRenderer)?.selectedPlanetIndex = selectedPlanetIndex
            }
        }, modifier = Modifier.fillMaxSize())

        // Кнопки внизу экрана
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                selectedPlanetIndex =
                    (selectedPlanetIndex - 1 + 8) % 8 // переключение влево
            }) {
                Text("←")
            }
            Button(onClick = {
                // Можно показать инфо о планете через Toast или Compose Text
            }) {
                Text("i")
            }
            Button(onClick = {
                selectedPlanetIndex =
                    (selectedPlanetIndex + 1) % 8 // переключение вправо
            }) {
                Text("→")
            }
        }
    }
}
