package com.example.nutrivision.data.repository

import com.example.nutrivision.data.remote.api.CommentService
import com.example.nutrivision.utils.toResult

class CommentRepository(
    private val commentService: CommentService
) {

    suspend fun deleteComment(commentId: Int) =
        commentService.deleteComment(commentId).toResult()
}