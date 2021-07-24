package com.stelmashchuk.radiot.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DetailsScreen(name: String, content: @Composable (PaddingValues) -> Unit) {
  AnimatedVisibility(visibleState = remember { MutableTransitionState(initialState = false) }
      .apply { targetState = true },
      modifier = Modifier,
      enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(initialAlpha = 0.3f),
      exit = slideOutHorizontally() + fadeOut()) {
    Scaffold(topBar = {
      TopAppBar(backgroundColor = Color.Black) {
        Text(text = name, style = MaterialTheme.typography.h4)
      }
    }, content = content)
  }
}