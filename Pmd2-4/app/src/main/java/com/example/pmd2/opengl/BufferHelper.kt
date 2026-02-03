package com.example.pmd2.opengl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object FloatBufferHelper {
    fun create(array: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(array.size * 4)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(array)
        fb.position(0)
        return fb
    }
}

object ShortBufferHelper {
    fun create(array: ShortArray): ShortBuffer {
        val bb = ByteBuffer.allocateDirect(array.size * 2)
        bb.order(ByteOrder.nativeOrder())
        val sb = bb.asShortBuffer()
        sb.put(array)
        sb.position(0)
        return sb
    }
}
