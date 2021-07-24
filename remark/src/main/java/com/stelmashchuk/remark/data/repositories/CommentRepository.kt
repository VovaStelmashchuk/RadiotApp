package com.stelmashchuk.remark.data.repositories

import com.stelmashchuk.remark.RemarkSettings
import com.stelmashchuk.remark.data.HttpConstants
import com.stelmashchuk.remark.data.RemarkService
import com.stelmashchuk.remark.data.Result
import com.stelmashchuk.remark.data.onFailure
import com.stelmashchuk.remark.data.onSuccess
import com.stelmashchuk.remark.data.pojo.CommentWrapper
import com.stelmashchuk.remark.data.pojo.Comments
import com.stelmashchuk.remark.data.pojo.VoteResponse
import com.stelmashchuk.remark.data.pojo.VoteType
import com.stelmashchuk.remark.data.runCatching
import retrofit2.HttpException

class NotAuthUser : Exception()
class CacheNotValid : Exception()
class TooManyRequests : Exception()

class CommentRepository(
    private val remarkService: RemarkService,
    private val userStorage: UserStorage,
    private val voteRequestHandler: VoteRequestHandler = VoteRequestHandler(),
) {

  private var cache: Comments? = null

  suspend fun getComments(
      postUrl: String,
      sort: String = RemarkSettings.defaultSorting,
      format: String = "tree",
  ): Result<Comments> {
    if (cache == null) {
      val result = Result.runCatching { remarkService.getComments(postUrl, sort, format) }
      result.getOrNull()?.also {
        cache = it
      }
      return result
    }

    return Result.success(requireNotNull(cache))
  }

  @Suppress("ReturnCount")
  suspend fun vote(
      commentId: String,
      postUrl: String,
      vote: VoteType,
  ): Result<Comments> {
    if (cache == null) {
      return Result.failure(CacheNotValid())
    }
    if (!userStorage.getCredential().isValid()) {
      return Result.failure(NotAuthUser())
    }
    val voteResponse = Result.runCatching { remarkService.vote(commentId, postUrl, vote.backendCode) }
    return voteRequestHandler.handleVoteResponse(requireNotNull(cache).comments, voteResponse, vote)
  }
}

class VoteRequestHandler {

  fun handleVoteResponse(
      comments: List<CommentWrapper>,
      voteResponse: Result<VoteResponse>,
      vote: VoteType,
  ): Result<Comments> {
    var result = Result.failure<Comments>(Throwable())
    voteResponse.onSuccess {
      val cache = Comments(copyComments(comments, it, vote))
      result = Result.success(cache)
    }
    voteResponse.onFailure { throwable ->
      result = voteErrorHandle(throwable)
    }
    return result
  }

  private fun voteErrorHandle(throwable: Throwable): Result<Comments> {
    if (throwable is HttpException) {
      return when (throwable.code()) {
        HttpConstants.UN_AUTH -> Result.failure(NotAuthUser())
        HttpConstants.TOO_MANY_REQUESTS -> Result.failure(TooManyRequests())
        else -> Result.failure(throwable)
      }
    }

    return Result.failure(throwable)
  }

  private fun copyComments(
      comments: List<CommentWrapper>,
      voteResponse: VoteResponse,
      voteType: VoteType,
  ): List<CommentWrapper> {
    return comments.map {
      if (it.comment.id == voteResponse.id) {
        CommentWrapper(it.comment.copy(score = voteResponse.score, vote = voteType.backendCode), it.replies)
      } else {
        CommentWrapper(it.comment, copyComments(it.replies, voteResponse, voteType))
      }
    }
  }
}
