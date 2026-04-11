package com.example.nutrivision.data.remote.response


import com.google.gson.annotations.SerializedName

data class PagePagination(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("total_items")
    val totalItems: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

data class CursorPagination(
    @SerializedName("next_cursor")
    val nextCursor: Cursor?,
    @SerializedName("has_more")
    val hasMore: Boolean
)

data class Cursor(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: String
)