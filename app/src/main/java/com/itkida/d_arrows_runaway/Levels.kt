package com.itkida.d_arrows_runaway

val level1Arrows = listOf(
    ArrowBlock(1, 3, 3, Direction.UP),
    ArrowBlock(2, 3, 4, Direction.RIGHT),
    ArrowBlock(3, 5, 4, Direction.DOWN),
    ArrowBlock(4, 5, 2, Direction.LEFT)
)

val level2Arrows = listOf(
    // A cluster in the middle
    ArrowBlock(id = 1, row = 10, col = 10, direction = Direction.UP),
    ArrowBlock(id = 2, row = 4, col = 5, direction = Direction.RIGHT),
    ArrowBlock(id = 3, row = 14, col = 8, direction = Direction.DOWN),
    ArrowBlock(id = 4, row = 13, col = 5, direction = Direction.LEFT),

    // Perimeter blockers
    ArrowBlock(id = 5, row = 2, col = 8, direction = Direction.RIGHT),
    ArrowBlock(id = 6, row = 6, col = 14, direction = Direction.UP),
    ArrowBlock(id = 7, row = 17, col = 8, direction = Direction.LEFT),
    ArrowBlock(id = 8, row = 13, col = 2, direction = Direction.DOWN),

    // Outliers
    ArrowBlock(id = 9, row = 1, col = 1, direction = Direction.DOWN),
    ArrowBlock(id = 10, row = 17, col = 17, direction = Direction.UP)
)

val level3Arrows = listOf(

    ArrowBlock(
        id = 1,
        row = 2, col = 4,
        cornerRow = 4, cornerCol = 4,
        direction = Direction.RIGHT
    ),

    ArrowBlock(id = 2, row = 7, col = 4, direction = Direction.RIGHT),

    ArrowBlock(
        id = 3,
        row = 12, col = 4,
        cornerRow = 10, cornerCol = 4,
        direction = Direction.RIGHT
    ),

    ArrowBlock(id = 4, row = 13, col = 4, direction = Direction.RIGHT),

    ArrowBlock(
        id = 5,
        row = 18, col = 4,
        cornerRow = 16, cornerCol = 4,
        direction = Direction.RIGHT
    ),

    ArrowBlock(id = 6, row = 2, col = 8, direction = Direction.DOWN),

    ArrowBlock(id = 7, row = 5, col = 8, direction = Direction.DOWN),

    ArrowBlock(id = 8, row = 8, col = 8, direction = Direction.DOWN),

    ArrowBlock(id = 9, row = 12, col = 8, direction = Direction.DOWN),

    ArrowBlock(id = 10, row = 16, col = 8, direction = Direction.DOWN)
)