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
    onDetailClick: (selectedIndex: Int) -> Unit = {},
    onLunaClick: (selectedIndex: Int) -> Unit = {}
) {
    val context = LocalContext.current

    var selectedPlanetIndex by remember { mutableStateOf(0) }

    var rendererRef by remember { mutableStateOf<MyGLRenderer?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

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

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                selectedPlanetIndex = (selectedPlanetIndex - 1 + 10) % 10
                rendererRef?.selectedPlanetIndex = selectedPlanetIndex
            }) {
                Text("←")
            }

            Button(onClick = {
                if (selectedPlanetIndex == 8 || selectedPlanetIndex == 9) {
                    onLunaClick(selectedPlanetIndex)
                } else {
                    onDetailClick(selectedPlanetIndex)
                }
            }) {
                Text("i")
            }

            Button(onClick = {
                selectedPlanetIndex = (selectedPlanetIndex + 1) % 10
                rendererRef?.selectedPlanetIndex = selectedPlanetIndex
            }) {
                Text("→")
            }
        }
    }
}