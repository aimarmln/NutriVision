package com.example.nutrivision.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.nutrivision.R
import kotlin.math.*

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var carbPercent = 40f
    private var proteinPercent = 30f
    private var fatPercent = 30f

    private val chartRect = RectF()
    private val strokeWidth = 80f

    private val carbPaint = createPaint(ContextCompat.getColor(context, R.color.carbs))
    private val proteinPaint = createPaint(ContextCompat.getColor(context, R.color.protein))
    private val fatPaint = createPaint(ContextCompat.getColor(context, R.color.fat))

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 48f
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.hanken_grotesk)
    }

    private fun createPaint(colorInt: Int): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.STROKE
            strokeWidth = this@DonutChartView.strokeWidth
            strokeCap = Paint.Cap.BUTT
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val chartSize = min(width, height) * 0.7f
        val centerX = width / 2f
        val centerY = height / 2f - 40f
        chartRect.set(
            centerX - chartSize / 2,
            centerY - chartSize / 2,
            centerX + chartSize / 2,
            centerY + chartSize / 2
        )

        val total = carbPercent + proteinPercent + fatPercent
        var startAngle = -90f

        // Draw arcs
        val carbSweep = -(carbPercent / total) * 360f
        val proteinSweep = -(proteinPercent / total) * 360f
        val fatSweep = -(fatPercent / total) * 360f

        canvas.drawArc(chartRect, startAngle, carbSweep, false, carbPaint)
        drawChip(canvas, startAngle + carbSweep / 2, carbPercent.toInt(), centerX, centerY)
        startAngle += carbSweep

        canvas.drawArc(chartRect, startAngle, proteinSweep, false, proteinPaint)
        drawChip(canvas, startAngle + proteinSweep / 2, proteinPercent.toInt(), centerX, centerY)
        startAngle += proteinSweep

        canvas.drawArc(chartRect, startAngle, fatSweep, false, fatPaint)
        drawChip(canvas, startAngle + fatSweep / 2, fatPercent.toInt(), centerX, centerY)
    }

    private fun drawChip(canvas: Canvas, angle: Float, percent: Int, cx: Float, cy: Float) {
        val radius = (chartRect.width() / 2f)
        val angleRad = Math.toRadians(angle.toDouble())
        val chipX = (cx + cos(angleRad) * (radius + 50)).toFloat()
        val chipY = (cy + sin(angleRad) * (radius + 50)).toFloat()

        val text = "$percent%"
        val padding = 40f
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val rectWidth = textBounds.width() + padding * 2
        val rectHeight = textBounds.height() + padding

        val rectF = RectF(
            chipX - rectWidth / 2,
            chipY - rectHeight / 2,
            chipX + rectWidth / 2,
            chipY + rectHeight / 2
        )

        val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(6f, 0f, 0f, Color.LTGRAY)
        }

        canvas.drawRoundRect(rectF, 30f, 30f, chipPaint)

        textPaint.color = Color.BLACK
        canvas.drawText(text, chipX, chipY + textBounds.height() / 2f - 5f, textPaint)
    }
}
