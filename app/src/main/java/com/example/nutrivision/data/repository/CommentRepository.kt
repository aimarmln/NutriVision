package com.example.nutrivision.data.repository

import com.example.nutrivision.data.remote.api.CommentService
import com.example.nutrivision.utils.toResult

class CommentRepository(
    private val commentService: CommentService
) {

    suspend fun deleteComment(commentId: String) =
        commentService.deleteComment(commentId).toResult()
}