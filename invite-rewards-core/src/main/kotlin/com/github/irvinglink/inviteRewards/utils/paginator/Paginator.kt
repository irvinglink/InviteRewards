package com.github.irvinglink.inviteRewards.utils.paginator

/**
 * Used to make page system of any object. Commonly used on chat or inventory system.
 */
class Paginator<E> {

    fun paginate(page: Int, perPage: Int, list: List<E>): PageResult<E> {
        require(perPage > 0) { "perPage must be greater than 0" }

        if (list.isEmpty()) {
            return PageResult(emptyList(), 1, 1, false, false)
        }

        val totalPages = (list.size + perPage - 1) / perPage
        val currentPage = page.coerceIn(1, totalPages)

        val fromIndex = (currentPage - 1) * perPage
        val toIndex = minOf(fromIndex + perPage, list.size)

        val items = list.subList(fromIndex, toIndex)

        return PageResult(
            items = items,
            currentPage = currentPage,
            totalPages = totalPages,
            hasNext = currentPage < totalPages,
            hasPrevious = currentPage > 1
        )
    }

}