package com.itkida.d_arrows_runaway

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class ArrowBlock(
    val id: Int,
    val row: Int,
    val col: Int,
    val direction: Direction,
    val animOffset: Animatable<Float, AnimationVector1D> = Animatable(0f),
    var isMoving: Boolean = false
) {
    var currentColor by mutableStateOf(Color.Black)
}