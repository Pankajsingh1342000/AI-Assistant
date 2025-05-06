package com.example.aiassistant.presentation.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.example.aiassistant.R
import kotlin.math.abs
import kotlin.random.Random

class MicWaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val barPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            var needsInvalidate = false
            for (i in barHeights.indices) {
                val oldHeight = barHeights[i]
                val newHeight = lerp(oldHeight, targetHeights[i], smoothingFactor)
                if (abs(oldHeight - newHeight) > 1f) {
                    barHeights[i] = newHeight
                    needsInvalidate = true
                }
            }

            if (needsInvalidate) {
                invalidate()
                postDelayed(this, 16) // ~60fps
            }
        }
    }

    private var barCount: Int = 5
    private var barHeights: FloatArray
    private var targetHeights: FloatArray
    private var gradient: LinearGradient? = null
    private val smoothingFactor = 0.25f  // Lower = smoother, Higher = snappier



    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.MicWaveformView, 0, 0).apply {
            try {
                barCount = getInt(R.styleable.MicWaveformView_barCount, 5)
            } finally {
                recycle()
            }
        }
        barHeights = FloatArray(barCount) { 0f }
        targetHeights = FloatArray(barCount) { 0f }
    }

    fun updateAmplitude(amplitude: Int) {
        val scale = (amplitude / 20000f).coerceIn(0.05f, 1f)

        if (barHeights.size != barCount) {
            barHeights = FloatArray(barCount) { 0f }
        }
        if (targetHeights.size != barCount) {
            targetHeights = FloatArray(barCount) { 0f }
        }

        for (i in targetHeights.indices) {
            val noise = Random.nextFloat()
            targetHeights[i] = height * scale * noise
        }

        removeCallbacks(updateRunnable)
        post(updateRunnable)
    }

    private fun lerp(start: Float, end: Float, factor: Float): Float {
        return start + (end - start) * factor
    }

    fun stop() {
        removeCallbacks(updateRunnable)
        barHeights.fill(0f)
        targetHeights.fill(0f)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        gradient = LinearGradient(
            0f, 0f, w.toFloat(), 0f,
            intArrayOf(Color.parseColor("#42A5F5"), Color.parseColor("#7E57C2")),
            null, Shader.TileMode.CLAMP
        )
        barPaint.shader = gradient
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val barWidth = width / (barCount * 1.5f)
        val spacing = barWidth / 2
        val radius = barWidth / 2f
        var x = spacing

        for (i in 0 until barCount) {
            val barHeight = barHeights[i].coerceAtLeast(8f)
            val top = (height / 2f) - (barHeight / 2f)
            val bottom = (height / 2f) + (barHeight / 2f)
            val rect = RectF(x, top, x + barWidth, bottom)
            canvas.drawRoundRect(rect, radius, radius, barPaint)
            x += barWidth + spacing
        }
    }
}