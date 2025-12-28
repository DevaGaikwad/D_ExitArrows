package com.itkida.d_arrows_runaway

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun ArrowGameBoard(gameState: GameState) {
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.aspectRatio(1f).fillMaxWidth().padding(16.dp)) {
        val boardSize = constraints.maxWidth.toFloat()
        val cellSize = boardSize / gameState.cols

        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures { offset ->
                val col = (offset.x / cellSize).toInt()
                val row = (offset.y / cellSize).toInt()
                gameState.arrows.find { it.row == row && it.col == col }?.let {
                    gameState.tryMoveArrow(it, scope, cellSize)
                }
            }
        }) {
            // 1. Draw Dots
            for (r in 0 until gameState.rows) {
                for (c in 0 until gameState.cols) {
                    drawCircle(Color.DarkGray.copy(0.3f), 4f, Offset(c * cellSize + cellSize/2, r * cellSize + cellSize/2))
                }
            }

            // 2. Draw Arrows
            gameState.arrows.forEach { arrow ->
                drawPrettyArrow(arrow, cellSize)
            }
        }
    }
}

private fun DrawScope.drawPrettyArrow(arrow: ArrowBlock, cellSize: Float) {
    val centerX = arrow.col * cellSize + cellSize / 2
    val centerY = arrow.row * cellSize + cellSize / 2
    val offset = arrow.animOffset.value

    // Directional slide
    val drawX = centerX + when(arrow.direction) { Direction.LEFT -> -offset; Direction.RIGHT -> offset; else -> 0f }
    val drawY = centerY + when(arrow.direction) { Direction.UP -> -offset; Direction.DOWN -> offset; else -> 0f }

    val degrees = when (arrow.direction) {
        Direction.UP -> -90f; Direction.DOWN -> 90f
        Direction.LEFT -> 180f; Direction.RIGHT -> 0f
    }

    val headHeight = cellSize * 0.3f
    val headWidth = cellSize * 0.35f
    val tailStartX = -cellSize / 2
    val tailEndX = (cellSize / 2) - headHeight

    withTransform({ rotate(degrees, Offset(drawX, drawY)) }) {
        // Draw Stem
        drawLine(
            color = arrow.currentColor,
            start = Offset(drawX + tailStartX, drawY),
            end = Offset(drawX + tailEndX, drawY),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
        // Draw Head
        val path = Path().apply {
            moveTo(drawX + cellSize / 2, drawY)
            lineTo(drawX + tailEndX, drawY - headWidth / 2)
            lineTo(drawX + tailEndX, drawY + headWidth / 2)
            close()
        }
        drawPath(path, color = arrow.currentColor, style = Fill)
    }
}