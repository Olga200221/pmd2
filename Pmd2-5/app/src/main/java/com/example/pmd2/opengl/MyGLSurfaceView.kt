package com.example.pmd2.opengl

import android.content.Context
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    val renderer: MyGLRenderer

    init {
        // Используем OpenGL ES 2.0
        setEGLContextClientVersion(2)

        renderer = MyGLRenderer(context)
        setRenderer(renderer)

        // Обновление кадра постоянно
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
