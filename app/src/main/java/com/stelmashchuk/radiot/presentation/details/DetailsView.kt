package com.stelmashchuk.radiot.presentation.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stelmashchuk.radiot.presentation.common.DetailsScreen
import com.stelmashchuk.remark.feature.CommentWidget

@Composable
fun DetailView(podcastNumber: Long?) {
  if (podcastNumber == null) {
    Text(text = "Error")
  } else {
    PodcastContent(podcastNumber)
  }
}

@Composable
private fun PodcastContent(podcastNumber: Long) {
  val viewModel = viewModel<DetailsViewModel>(factory = DetailsViewModelFactory(podcastNumber))

  val podcast by viewModel.podcast.observeAsState()
  podcast?.let { episode ->
    DetailsScreen(name = episode.title) {
      Column(modifier = Modifier
          .padding(4.dp)
          .fillMaxWidth()
      ) {
        Text(text = episode.topics, style = MaterialTheme.typography.body1)
        CommentWidget(postUrl = episode.url)
      }
    }
  }
}
