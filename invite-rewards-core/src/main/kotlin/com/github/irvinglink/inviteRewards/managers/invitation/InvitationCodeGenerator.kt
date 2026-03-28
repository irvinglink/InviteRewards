package com.github.irvinglink.inviteRewards.managers.invitation

object InviteCodeGenerator {

    private const val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun generate(segmentLength: Int = 4, segments: Int = 2): String {
        return (1..segments)
            .joinToString("-") {
                (1..segmentLength)
                    .map { CHARS.random() }
                    .joinToString("")
            }
    }

    fun generateFlat(length: Int): String {
        return (1..length)
            .map { CHARS.random() }
            .joinToString("")
    }
}