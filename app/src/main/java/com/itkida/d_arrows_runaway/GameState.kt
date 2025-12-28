package com.itkida.d_arrows_runaway

import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class GameState(val rows: Int = 20, val cols: Int = 20) {
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

        // VISUAL REALITY: The arrow spans 3 cells (Tail -> Mid -> Tip)
        // because we draw it with length = cellSize * 2.
        val arrowVisualLen = cellSize * 2

        val hitResult = getHitResult(arrow)

        if (hitResult == null) {
            // SUCCESS
            arrow.currentColor = Color(0xFF4CAF50)
            arrow.isMoving = true

            val distToCornerCells = if (arrow.cornerRow != null) {
                abs(arrow.row - arrow.cornerRow) + abs(arrow.col - (arrow.cornerCol ?: arrow.col))
            } else 0
            val totalFlyDistance = (distToCornerCells * cellSize) + (cols * cellSize)

            scope.launch {
                arrow.animOffset.animateTo(totalFlyDistance, tween(1500))
                arrows.remove(arrow)
                if (arrows.isEmpty()) isLevelComplete = true
            }
        } else {
            // BLOCKED
            val (cellsToBlocker, blocker) = hitResult
            scope.launch {
                arrow.isMoving = true
                arrow.currentColor = Color.Red

                // BOUNCE CALC: Move until our TIP touches the BLOCKER.
                // Distance to blocker = cellsToBlocker * cellSize
                // Our length = arrowVisualLen
                // Stop point = Distance - Length
                val movePx = (cellsToBlocker * cellSize) - arrowVisualLen
                val safeMovePx = movePx.coerceAtLeast(0f)

                arrow.animOffset.animateTo(safeMovePx, tween(300))

                launch {
                    repeat(2) {
                        blocker.currentColor = Color(0xFF8B0000)
                        delay(150)
                        blocker.currentColor = Color.Black
                        delay(150)
                    }
                }
                arrow.animOffset.animateTo(0f, tween(300))
                arrow.isMoving = false
            }
        }
    }

    private fun getHitResult(arrow: ArrowBlock): Pair<Int, ArrowBlock>? {
        var steps = 0
        var r = arrow.row
        var c = arrow.col

        // 1. Check Path to Corner
        if (arrow.cornerRow != null && arrow.cornerCol != null) {
            val dr = (arrow.cornerRow - r).coerceIn(-1, 1)
            val dc = (arrow.cornerCol - c).coerceIn(-1, 1)
            while (r != arrow.cornerRow || c != arrow.cornerCol) {
                steps++
                r += dr; c += dc
                val blocker = checkCellOccupied(r, c, arrow.id)
                if (blocker != null) return Pair(steps, blocker)
            }
        }

        // 2. Check Path to Exit
        while (true) {
            steps++
            when (arrow.direction) {
                Direction.UP -> r--; Direction.DOWN -> r++
                Direction.LEFT -> c--; Direction.RIGHT -> c++
            }
            if (r !in 0 until rows || c !in 0 until cols) return null

            val blocker = checkCellOccupied(r, c, arrow.id)
            if (blocker != null) return Pair(steps, blocker)
        }
    }

    // --- THE FIX: CHECK EVERY CELL THE ARROW OCCUPIES ---
    private fun checkCellOccupied(targetR: Int, targetC: Int, ignoreId: Int): ArrowBlock? {
        return arrows.find { other ->
            if (other.id == ignoreId || other.isMoving) return@find false

            // 1. Check TAIL
            if (other.row == targetR && other.col == targetC) return@find true

            // 2. Check CORNER (if exists)
            val cornerR = other.cornerRow ?: other.row
            val cornerC = other.cornerCol ?: other.col

            // If it's an L-shape, check the corner and the path to it
            if (other.cornerRow != null) {
                if (targetR == cornerR && targetC == cornerC) return@find true
                // (Optional: You could check the path between Start and Corner here too,
                // but usually Corner + Tail covers small Ls)
            }

            // 3. Check MIDDLE and TIP (The "Line" and the "Point")
            // Visual length is 2 cells *after* the corner/start.
            // So we check +1 step and +2 steps.
            val dX = getDirX(other.direction)
            val dY = getDirY(other.direction)

            // Cell +1 (The "Line" or Middle)
            val midR = cornerR + dY
            val midC = cornerC + dX
            if (midR == targetR && midC == targetC) return@find true

            // Cell +2 (The "Tip")
            val tipR = cornerR + (dY * 2)
            val tipC = cornerC + (dX * 2)
            if (tipR == targetR && tipC == targetC) return@find true

            false
        }
    }

    private fun getDirX(d: Direction) = when(d) { Direction.LEFT->-1; Direction.RIGHT->1; else->0 }
    private fun getDirY(d: Direction) = when(d) { Direction.UP->-1; Direction.DOWN->1; else->0 }
}