package com.itkida.d_arrows_runaway

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.atan2

@Composable
fun ArrowGameBoard(gameState: GameState) {
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val cellSize = constraints.maxWidth.toFloat() / gameState.cols

        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures { offset ->
                val tapCol = (offset.x / cellSize).toInt()
                val tapRow = (offset.y / cellSize).toInt()

                // HITBOX LOGIC: Check Tail, Corner, or Projected Head
                val clickedArrow = gameState.arrows.find { arrow ->
                    // 1. Click on Tail
                    if (arrow.row == tapRow && arrow.col == tapCol) return@find true
                    // 2. Click on Corner
                    if (arrow.cornerRow == tapRow && arrow.cornerCol == tapCol) return@find true

                    // 3. Click on Head (Visual L-shape tip)
                    // Since visual length is cellSize * 2, the tip is 2 cells away
                    val isLShape = arrow.cornerRow != null

                    if (isLShape) {
                        // Start from corner, move 2 steps in direction
                        val headR = arrow.cornerRow!! + (getDirY(arrow.direction) * 2).toInt()
                        val headC = arrow.cornerCol!! + (getDirX(arrow.direction) * 2).toInt()

                        // Also check the cell in between (since it's size 2)
                        val midR = arrow.cornerRow!! + getDirY(arrow.direction).toInt()
                        val midC = arrow.cornerCol!! + getDirX(arrow.direction).toInt()

                        if ((headR == tapRow && headC == tapCol) || (midR == tapRow && midC == tapCol)) return@find true
                    } else {
                        // Straight Arrow: Start from tail, move 2 steps
                        val headR = arrow.row + (getDirY(arrow.direction) * 2).toInt()
                        val headC = arrow.col + (getDirX(arrow.direction) * 2).toInt()

                        // Check middle cell too
                        val midR = arrow.row + getDirY(arrow.direction).toInt()
                        val midC = arrow.col + getDirX(arrow.direction).toInt()

                        if ((headR == tapRow && headC == tapCol) || (midR == tapRow && midC == tapCol)) return@find true
                    }
                    false
                }
                clickedArrow?.let { gameState.tryMoveArrow(it, scope, cellSize) }
            }
        }) {
            drawGridDots(gameState.rows, gameState.cols, cellSize)
            gameState.arrows.forEach { arrow -> drawSnakeArrow(arrow, cellSize) }
        }
    }
}

private fun DrawScope.drawSnakeArrow(arrow: ArrowBlock, cellSize: Float) {
    val start = Offset(arrow.col * cellSize + cellSize / 2, arrow.row * cellSize + cellSize / 2)
    val corner = if (arrow.cornerRow != null)
        Offset(arrow.cornerCol!! * cellSize + cellSize / 2, arrow.cornerRow!! * cellSize + cellSize / 2)
    else null

    val distToCorner = if (corner != null) (abs(start.x - corner.x) + abs(start.y - corner.y)) else 0f

    // Length is Corner + 2 Cells
    val staticLength = if (corner != null) distToCorner + (cellSize * 2) else cellSize * 2

    val headDist = arrow.animOffset.value + staticLength
    val tailDist = arrow.animOffset.value

    val initialDirection = getStartDirection(arrow)

    // Arrow Head Dimensions (Must match drawArrowHead)
    val headHeight = cellSize * 0.6f

    // FIX 1: STOP THE STEM EARLY
    // We stop the line 'headHeight' pixels before the actual tip,
    // so it connects to the base of the triangle, not the sharp point.
    val stemEndDist = (headDist - headHeight).coerceAtLeast(tailDist)

    // Calculate Positions
    val headPos = getPositionOnPath(headDist, start, corner, initialDirection, arrow.direction, cellSize)
    val stemEndPos = getPositionOnPath(stemEndDist, start, corner, initialDirection, arrow.direction, cellSize) // Use stemEndDist here
    val tailPos = getPositionOnPath(tailDist, start, corner, initialDirection, arrow.direction, cellSize)

    // Draw Stem (Line)
    val stemPath = Path().apply {
        moveTo(tailPos.x, tailPos.y)

        // Logic to draw through corner if needed
        // Note: We use stemEndDist here to see if the stem crosses the corner
        if (corner != null && stemEndDist > headPos.distToCorner && tailDist < headPos.distToCorner) {
            lineTo(corner.x, corner.y)
        }

        // Draw to the BASE of the arrow head, not the tip
        lineTo(stemEndPos.x, stemEndPos.y)
    }

    drawPath(
        path = stemPath,
        color = arrow.currentColor,
        style = Stroke(width = 10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Draw Triangle (Tip)
    drawArrowHead(arrow.currentColor, headPos.x, headPos.y, headPos.angle, cellSize)
}

// --- HELPERS ---

private fun getStartDirection(arrow: ArrowBlock): Direction {
    if (arrow.cornerRow != null && arrow.cornerCol != null) {
        val dRow = arrow.cornerRow - arrow.row
        val dCol = arrow.cornerCol - arrow.col
        return when {
            dRow > 0 -> Direction.DOWN; dRow < 0 -> Direction.UP
            dCol > 0 -> Direction.RIGHT; dCol < 0 -> Direction.LEFT
            else -> arrow.direction
        }
    }
    return arrow.direction
}

data class PathPos(val x: Float, val y: Float, val angle: Float, val distToCorner: Float)

private fun getPositionOnPath(dist: Float, start: Offset, corner: Offset?, startDir: Direction, endDir: Direction, cellSize: Float): PathPos {
    val distToCorner = if (corner != null) (abs(start.x - corner.x) + abs(start.y - corner.y)) else 0f

    if (corner == null || dist <= distToCorner) {
        // SEGMENT 1: Start -> Corner
        // We multiply by 50 just to ensure the target point is far enough for straight arrows
        val target = corner ?: Offset(start.x + getDirX(startDir)*cellSize*50, start.y + getDirY(startDir)*cellSize*50)
        val angle = if (corner == null) getDirAngle(startDir) else getAngle(start, corner)

        val ratio = if (distToCorner > 0) (dist / distToCorner).coerceIn(0f, 1f) else 0f

        // Linear Interpolation
        val currX = if (distToCorner == 0f) start.x + getDirX(startDir)*dist else lerp(start.x, target.x, ratio)
        val currY = if (distToCorner == 0f) start.y + getDirY(startDir)*dist else lerp(start.y, target.y, ratio)

        return PathPos(currX, currY, angle, distToCorner)
    } else {
        // SEGMENT 2: Corner -> Head
        val distAfter = dist - distToCorner
        val currX = corner.x + getDirX(endDir) * distAfter
        val currY = corner.y + getDirY(endDir) * distAfter
        return PathPos(currX, currY, getDirAngle(endDir), distToCorner)
    }
}

private fun DrawScope.drawArrowHead(color: Color, x: Float, y: Float, deg: Float, cellSize: Float) {
    // These sizes must match the calculation in drawSnakeArrow
    val h = cellSize * 0.6f
    val w = cellSize * 0.65f

    withTransform({ rotate(deg, Offset(x, y)) }) {
        val path = Path().apply {
            moveTo(x, y) // Tip
            lineTo(x - h, y - w / 2) // Bottom Left
            lineTo(x - h, y + w / 2) // Bottom Right
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}

private fun DrawScope.drawGridDots(rows: Int, cols: Int, cellSize: Float) {
    for (r in 0 until rows) for (c in 0 until cols)
        drawCircle(Color.DarkGray.copy(0.3f), 4f, Offset(c * cellSize + cellSize / 2, r * cellSize + cellSize / 2))
}

fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
fun getAngle(a: Offset, b: Offset) = Math.toDegrees(atan2((b.y - a.y).toDouble(), (b.x - a.x).toDouble())).toFloat()
fun getDirX(d: Direction) = when(d) { Direction.LEFT->-1f; Direction.RIGHT->1f; else->0f }
fun getDirY(d: Direction) = when(d) { Direction.UP->-1f; Direction.DOWN->1f; else->0f }
fun getDirAngle(d: Direction) = when(d) { Direction.UP->-90f; Direction.DOWN->90f; Direction.LEFT->180f; else->0f }