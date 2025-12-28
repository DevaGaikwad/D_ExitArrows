package com.itkida.d_arrows_runaway

import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameState(val rows: Int = 10, val cols: Int = 10) {
    val arrows = mutableStateListOf<ArrowBlock>()
    var isLevelComplete by mutableStateOf(false)
    var isLoading by mutableStateOf(true)

    fun loadLevel(newArrows: List<ArrowBlock>) {
        isLoading = true
        isLevelComplete = false
        arrows.clear()
        arrows.addAll(newArrows)
        isLoading = false
    }

    fun tryMoveArrow(arrow: ArrowBlock, scope: CoroutineScope, cellSize: Float) {
        if (arrow.isMoving) return

        val hitResult = getHitResult(arrow)

        if (hitResult == null) {
            // SUCCESS: Path is clear, fly away slowly
            arrow.currentColor = Color(0xFF4CAF50) // Green
            arrow.isMoving = true
            scope.launch {
                arrow.animOffset.animateTo(2000f, tween(1200))
                arrows.remove(arrow)
                if (arrows.isEmpty()) isLevelComplete = true
            }
        } else {
            // BLOCKED: Bounce and stay Red
            val (distance, blocker) = hitResult
            scope.launch {
                arrow.isMoving = true
                arrow.currentColor = Color.Red

                // Calculate distance to touch (distance * cellSize minus one cell for the arrow itself)
                val moveDistance = (distance * cellSize) - cellSize

                // 1. Move to touch the blocker
                arrow.animOffset.animateTo(moveDistance, tween(250))

                // 2. Blink the blocker: Black -> Dark Red -> Black
                launch {
                    val darkRed = Color(0xFF8B0000)
                    repeat(2) {
                        blocker.currentColor = darkRed
                        delay(150)
                        blocker.currentColor = Color.Black
                        delay(150)
                    }
                }

                // 3. Reverse to original position
                arrow.animOffset.animateTo(0f, tween(250))
                arrow.isMoving = false
            }
        }
    }

    private fun getHitResult(arrow: ArrowBlock): Pair<Int, ArrowBlock>? {
        var steps = 0
        var r = arrow.row
        var c = arrow.col
        while (true) {
            steps++
            when (arrow.direction) {
                Direction.UP -> r--; Direction.DOWN -> r++
                Direction.LEFT -> c--; Direction.RIGHT -> c++
            }
            if (r !in 0 until rows || c !in 0 until cols) return null
            val blocker = arrows.find { it.row == r && it.col == c && !it.isMoving }
            if (blocker != null) return Pair(steps, blocker)
        }
    }
}