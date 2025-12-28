package com.itkida.d_arrows_runaway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize our state logic
        val gameState = GameState()

        setContent {
            MaterialTheme {
                Surface(color = Color(0xFFF0F0F0),) {
                    GameScreen(gameState)
                }
            }
        }
    }
}

val level2Arrows = listOf(
    // A cluster in the middle
    ArrowBlock(id = 1, row = 4, col = 4, direction = Direction.UP),
    ArrowBlock(id = 2, row = 4, col = 5, direction = Direction.RIGHT),
    ArrowBlock(id = 3, row = 5, col = 5, direction = Direction.DOWN),
    ArrowBlock(id = 4, row = 5, col = 4, direction = Direction.LEFT),

    // Perimeter blockers
    ArrowBlock(id = 5, row = 2, col = 4, direction = Direction.RIGHT),
    ArrowBlock(id = 6, row = 4, col = 7, direction = Direction.UP),
    ArrowBlock(id = 7, row = 7, col = 5, direction = Direction.LEFT),
    ArrowBlock(id = 8, row = 5, col = 2, direction = Direction.DOWN),

    // Outliers
    ArrowBlock(id = 9, row = 1, col = 1, direction = Direction.DOWN),
    ArrowBlock(id = 10, row = 8, col = 8, direction = Direction.UP)
)

@Composable
fun GameScreen(gameState: GameState) {
    val scope = rememberCoroutineScope()
    // Track which level the user is on
    var currentLevelNumber by remember { mutableStateOf(1) }

    // Load the level based on the number
    LaunchedEffect(currentLevelNumber) {
        val selectedLevel = when (currentLevelNumber) {
            1 -> listOf(
                ArrowBlock(1, 3, 3, Direction.UP),
                ArrowBlock(2, 3, 4, Direction.RIGHT),
                ArrowBlock(3, 5, 4, Direction.DOWN),
                ArrowBlock(4, 5, 2, Direction.LEFT)
            )
            2 -> level2Arrows // The list we created above
//            3 -> level3Arrows
            else -> emptyList()
        }
        gameState.loadLevel(selectedLevel)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Level $currentLevelNumber",
            style = MaterialTheme.typography.headlineMedium
        )

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (gameState.isLevelComplete) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LEVEL CLEAR!",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { currentLevelNumber++ }) {
                        Text("Next Level")
                    }
                }
            } else {
                ArrowGameBoard(gameState)
            }
        }

        Row {
            Button(onClick = { gameState.loadLevel(gameState.arrows.toList()) }) {
                Text("Reset")
            }
        }
    }
}
