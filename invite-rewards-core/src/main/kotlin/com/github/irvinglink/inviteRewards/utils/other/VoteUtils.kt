package com.github.irvinglink.inviteRewards.utils.other

class VoteSession<T> {

    private val votes = mutableMapOf<T, Int>()

    var isVotingOpen: Boolean = false
        private set

    fun openVoting() {
        isVotingOpen = true
    }

    fun closeVoting() {
        isVotingOpen = false
    }

    fun castVote(item: T): Boolean {
        if (!isVotingOpen) return false

        votes[item] = getVotes(item) + 1
        return true
    }

    fun removeVote(item: T): Boolean {
        val currentVotes = votes[item] ?: return false

        if (currentVotes > 1) {
            votes[item] = currentVotes - 1
        } else {
            votes.remove(item)
        }

        return true
    }

    fun getVotes(item: T): Int {
        return votes[item] ?: 0
    }

    fun hasVotes(): Boolean {
        return votes.isNotEmpty()
    }

    fun totalVotes(): Int {
        return votes.values.sum()
    }

    fun hasTie(): Boolean {
        val maxVotes = votes.values.maxOrNull() ?: return false
        return votes.values.count { it == maxVotes } > 1
    }

    fun getWinners(): List<T> {
        val maxVotes = votes.values.maxOrNull() ?: return emptyList()
        return votes
            .filterValues { it == maxVotes }
            .keys
            .toList()
    }

    fun getWinnerOrNull(): T? {
        val winners = getWinners()
        return if (winners.size == 1) winners.first() else null
    }

    fun getWinner(defaultValue: T): T {
        return getWinnerOrNull() ?: defaultValue
    }

    fun getSortedResultsDescending(): List<Pair<T, Int>> {
        return votes.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }
    }

    fun getPercentage(item: T): Double {
        val total = totalVotes()
        if (total == 0) return 0.0

        return (getVotes(item).toDouble() / total.toDouble()) * 100.0
    }

    fun clearVotes() {
        votes.clear()
    }

    fun reset() {
        votes.clear()
        isVotingOpen = false
    }
}