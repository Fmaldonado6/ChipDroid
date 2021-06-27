package com.fmaldonado.chip8.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.floor

class EmulatorScreen(context: Context, attr: AttributeSet) : View(context, attr) {

    var display: List<Byte>? = null
    var scale = 0F

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val pointPaintBG = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {

            Log.d("Draw", "DRAWING")
            display?.let {
                for (i in it.indices) {
                    if (it[i].toInt() == 0)
                        continue
                    val x = (i % 64) * scale
                    val y: Float = (floor((i / 64).toFloat())) * scale
                    canvas.drawRect(
                        x,
                        y,
                        x.toFloat() + scale,
                        y.toFloat() + scale,
                        pointPaint
                    );
                }
            }
        }

    }


}