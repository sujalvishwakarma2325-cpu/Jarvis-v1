package com.jarvis.v3.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import android.view.animation.DecelerateInterpolator

class JarvisOrbView(
    context: Context
) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var pulse = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1800L
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = DecelerateInterpolator()

        addUpdateListener {
            pulse = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        val baseRadius =
            minOf(width, height) * 0.22f

        val glowRadius =
            baseRadius * (1.8f + pulse * 0.35f)

        // Outer glow
        paint.shader = RadialGradient(
            centerX,
            centerY,
            glowRadius,
            intArrayOf(
                JarvisTheme.ORB_GLOW,
                0x552AC7FF,
                0x002AC7FF
            ),
            floatArrayOf(
                0f,
                0.45f,
                1f
            ),
            Shader.TileMode.CLAMP
        )

        canvas.drawCircle(
            centerX,
            centerY,
            glowRadius,
            paint
        )

        // Main orb
        paint.shader = RadialGradient(
            centerX - baseRadius * 0.3f,
            centerY - baseRadius * 0.3f,
            baseRadius * 1.4f,
            intArrayOf(
                0xFFFFFFFF.toInt(),
                JarvisTheme.PRIMARY,
                JarvisTheme.PRIMARY_DARK
            ),
            floatArrayOf(
                0f,
                0.35f,
                1f
            ),
            Shader.TileMode.CLAMP
        )

        canvas.drawCircle(
            centerX,
            centerY,
            baseRadius,
            paint
        )

        paint.shader = null

        // Inner core
        paint.color = 0xFF081521.toInt()

        canvas.drawCircle(
            centerX,
            centerY,
            baseRadius * 0.48f,
            paint
        )

        // Small center light
        paint.color = JarvisTheme.PRIMARY

        canvas.drawCircle(
            centerX,
            centerY,
            baseRadius * (0.10f + pulse * 0.03f),
            paint
        )
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }
}
