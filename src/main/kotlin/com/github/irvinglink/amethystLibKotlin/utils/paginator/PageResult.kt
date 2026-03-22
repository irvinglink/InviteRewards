package com.github.irvinglink.amethystLibKotlin.utils.paginator

data class PageResult<E>(
    val items: List<E>,
    val currentPage: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)