package com.stelmashchuk.remark.feature.comments

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.stelmashchuk.remark.common.FullSizeProgress
import com.stelmashchuk.remark.data.pojo.VoteType
import com.stelmashchuk.remark.feature.comments.mappers.ScoreView
import dev.jeziellago.compose.markdowntext.MarkdownText

data class CommentUiModel(
    val author: CommentAuthorUiModel,
    val text: String,
    val level: Int,
    val score: ScoreUiModel,
    val time: String,
    val commentId: String,
)

data class CommentAuthorUiModel(
    val name: String,
    val avatar: String,
)

data class ScoreUiModel(
    val score: String,
    val color: Int,
    @DrawableRes val upRes: Int,
    @DrawableRes val downRes: Int,
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CommentView(postUrl: String, level: Int = 1) {
  val viewModel: CommentsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST")
      return CommentsViewModel(postUrl = postUrl) as T
    }
  })

  val data by viewModel.commentsLiveData.observeAsState()
  val message by viewModel.messageLiveData.observeAsState()

  data?.let { state ->
    when (state) {
      is ViewState.Data -> {
        Scaffold(
            snackbarHost = {
              message?.let {
                Snackbar {
                  Text(text = stringResource(id = it.id))
                }
              }
            }
        ) {
          CommentData(comments = state.data, viewModel::vote)
        }
      }
      ViewState.Loading -> FullSizeProgress()
    }
  }
}

@ExperimentalAnimationApi
@Composable
fun CommentData(comments: List<CommentUiModel>, vote: (String, VoteType) -> Unit) {
  AnimatedVisibility(visibleState = remember { MutableTransitionState(initialState = false) }
      .apply { targetState = true },
      modifier = Modifier,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(initialAlpha = 0.3f),
      exit = ExitTransition.None) {
    CommentContent(comments = comments, vote)
  }
}

@Composable
fun CommentContent(comments: List<CommentUiModel>, onVote: (commentId: String, voteType: VoteType) -> Unit) {
  LazyColumn(modifier = Modifier
      .padding(8.dp)
      .fillMaxSize()) {
    items(comments) { comment ->
      CommentView(comment, onVote)
      Divider()
    }
  }
}

@Composable
fun CommentView(comment: CommentUiModel, onVote: (commentId: String, voteType: VoteType) -> Unit) {
  @Suppress("MagicNumber")
  Column(modifier = Modifier.padding(start = (8 * comment.level).dp)) {
    CommentAuthor(author = comment.author)
    MarkdownText(markdown = comment.text)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = comment.time, fontSize = 12.sp)
      ScoreView(score = comment.score) { voteType ->
        onVote(comment.commentId, voteType)
      }
    }
  }
}

@Composable
fun CommentAuthor(author: CommentAuthorUiModel) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Image(
        painter = rememberCoilPainter(
            request = author.avatar,
            requestBuilder = fun ImageRequest.Builder.(_: IntSize): ImageRequest.Builder {
              return transformations(CircleCropTransformation())
            },
            shouldRefetchOnSizeChange = { _, _ -> true },
        ),
        contentDescription = "Avatar ${author.name}",
        modifier = Modifier.padding(4.dp),
    )
    Text(text = author.name, style = MaterialTheme.typography.subtitle2)
  }
}
