package com.fmaldonado.chip8.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class EmulatorScreen(context: Context, attr: AttributeSet) : View(context,attr) {

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            canvas.drawLine(0f, 0f, 10f, 10f, pointPaint);
        }

    }

}