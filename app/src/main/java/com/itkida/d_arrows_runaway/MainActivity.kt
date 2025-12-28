package com.itkida.d_arrows_runaway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gameState = GameState() // Your GameState logic

        setContent {
            MaterialTheme {
                Surface(color = Color(0xFFF0F0F0)) {
                    GameScreen(gameState)
                }
            }
        }
    }
}

// --- HELPER TO GET FRESH LEVEL DATA ---
// We use a function instead of a global variable to ensure we get
// new, clean ArrowBlock objects every time we call it.
fun getLevelArrows(level: Int): List<ArrowBlock> {
    val rawList = when (level) {
        1 -> level1Arrows
        2 -> level2Arrows // Ensure level2Arrows is defined below
        3 -> level3Arrows // Ensure level3Arrows is defined
        else -> emptyList()
    }

    // CRITICAL FIX: Create a COPY of every arrow.
    // Otherwise, resetting reloads the 'moved' or 'red' arrows from memory.
    return rawList.map {
        it.copy(
            animOffset = androidx.compose.animation.core.Animatable(0f),
            isMoving = false
            // currentColor resets automatically because it's a var inside the class,
            // but copying the data object creates a fresh instance.
        )
    }
}

@Composable
fun GameScreen(gameState: GameState) {
    // Track current level
    var currentLevelNumber by remember { mutableStateOf(1) }

    // 1. Load Level Automatically when 'currentLevelNumber' changes
    LaunchedEffect(currentLevelNumber) {
        gameState.loadLevel(getLevelArrows(currentLevelNumber))
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Header
        Text(
            text = "Level $currentLevelNumber",
            style = MaterialTheme.typography.headlineMedium
        )

        // Game Board Area
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

        // Footer / Reset Button
        Row {
            Button(
                onClick = {
                    // FIX: Reloads the CURRENT level using the helper
                    currentLevelNumber = 2
                    gameState.loadLevel(getLevelArrows(currentLevelNumber))
                }
            ) {
                Text("Level 1")
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                onClick = {
                    // FIX: Reloads the CURRENT level using the helper
                    gameState.loadLevel(getLevelArrows(currentLevelNumber))
                }
            ) {
                Text("Reset")
            }

        }
    }
}