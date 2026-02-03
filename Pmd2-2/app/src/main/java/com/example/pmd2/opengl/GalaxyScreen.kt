package com.example.pmd2.opengl

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun GalaxyScreen() {
    AndroidView(factory = { context ->
        MyGLSurfaceView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
    })
}
