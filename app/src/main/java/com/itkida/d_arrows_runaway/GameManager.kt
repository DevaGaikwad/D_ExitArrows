package com.itkida.d_arrows_runaway

class GameManager(val rows: Int, val cols: Int) {
    var arrows = mutableListOf<ArrowBlock>()

    // Check if a grid cell is occupied
    fun isOccupied(row: Int, col: Int): Boolean {
        if (row !in 0 until rows || col !in 0 until cols) return false
        return arrows.any { it.row == row && it.col == col }
    }

    // Logic to move the arrow out of the grid
    fun tryMoveArrow(arrow: ArrowBlock, onComplete: () -> Unit) {
        // In this game, arrows fly off the screen if their path is clear
        if (isPathClear(arrow)) {
            arrows.remove(arrow)
            onComplete()
        }
    }

    private fun isPathClear(arrow: ArrowBlock): Boolean {
        var r = arrow.row
        var c = arrow.col

        while (true) {
            when (arrow.direction) {
                Direction.UP -> r--
                Direction.DOWN -> r++
                Direction.LEFT -> c--
                Direction.RIGHT -> c++
            }

            // If it reaches the boundary, the path is clear!
            if (r !in 0 until rows || c !in 0 until cols) return true
            // If it hits another arrow, it's blocked
            if (isOccupied(r, c)) return false
        }
    }
}