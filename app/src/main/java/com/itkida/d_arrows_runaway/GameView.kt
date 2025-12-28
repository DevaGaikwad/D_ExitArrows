package com.itkida.d_arrows_runaway

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GameView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gameManager = GameManager(10, 10) // 10x10 grid
    private var cellSize = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cellSize = w.toFloat() / gameManager.cols
    }

    override fun onDraw(canvas: Canvas) {
        // 1. Draw Grid Dots
        paint.color = Color.LTGRAY
        for (r in 0 until gameManager.rows) {
            for (c in 0 until gameManager.cols) {
                canvas.drawCircle(c * cellSize + cellSize/2, r * cellSize + cellSize/2, 5f, paint)
            }
        }

        // 2. Draw Arrows
        paint.color = Color.BLACK
        paint.strokeWidth = 5f
        for (arrow in gameManager.arrows) {
            drawArrow(canvas, arrow)
        }
    }

    private fun drawArrow(canvas: Canvas, arrow: ArrowBlock) {
        val x = arrow.col * cellSize + cellSize / 2
        val y = arrow.row * cellSize + cellSize / 2
        // Simple line drawing logic based on arrow.direction
        // (You can replace this with bitmap icons later)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / cellSize).toInt()
            val row = (event.y / cellSize).toInt()

            val clickedArrow = gameManager.arrows.find { it.row == row && it.col == col }
            clickedArrow?.let {
                gameManager.tryMoveArrow(it) {
                    invalidate() // Redraw the screen
                }
            }
        }
        return true
    }
}

