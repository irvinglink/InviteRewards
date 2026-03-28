package com.github.irvinglink.inviteRewards.utils.time

object TimeUtils {

    private const val SECOND_IN_MILLIS = 1000L
    private const val MINUTE_IN_MILLIS = 60 * SECOND_IN_MILLIS
    private const val HOUR_IN_MILLIS = 60 * MINUTE_IN_MILLIS
    private const val DAY_IN_MILLIS = 24 * HOUR_IN_MILLIS

    fun toMillis(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0): Long {
        require(days >= 0) { "days cannot be negative" }
        require(hours >= 0) { "hours cannot be negative" }
        require(minutes >= 0) { "minutes cannot be negative" }
        require(seconds >= 0) { "seconds cannot be negative" }

        return (days * DAY_IN_MILLIS) +
                (hours * HOUR_IN_MILLIS) +
                (minutes * MINUTE_IN_MILLIS) +
                (seconds * SECOND_IN_MILLIS)
    }

    fun formatDuration(maxTime: Long, minTime: Long): String {
        return formatDuration(maxTime - minTime)
    }

    fun formatShortDuration(maxTime: Long, minTime: Long): String {
        return formatShortDuration(maxTime - minTime)
    }

    fun formatDuration(durationMillis: Long): String {
        val parts = splitDuration(durationMillis)
        val result = mutableListOf<String>()

        if (parts.days > 0) result += "${parts.days} Day${if (parts.days != 1) "s" else ""}"
        if (parts.hours > 0) result += "${parts.hours} Hour${if (parts.hours != 1) "s" else ""}"
        if (parts.minutes > 0) result += "${parts.minutes} Minute${if (parts.minutes != 1) "s" else ""}"
        if (parts.seconds > 0) result += "${parts.seconds} Second${if (parts.seconds != 1) "s" else ""}"

        return if (result.isEmpty()) "0 Seconds" else result.joinToString(" ")
    }

    fun formatShortDuration(durationMillis: Long): String {
        val parts = splitDuration(durationMillis)
        val result = mutableListOf<String>()

        if (parts.days > 0) result += "${parts.days}d"
        if (parts.hours > 0) result += "${parts.hours}h"
        if (parts.minutes > 0) result += "${parts.minutes}m"
        if (parts.seconds > 0) result += "${parts.seconds}s"

        return if (result.isEmpty()) "0s" else result.joinToString(" ")
    }

    private fun splitDuration(durationMillis: Long): TimeParts {
        val safeMillis = durationMillis.coerceAtLeast(0L)

        val days = (safeMillis / DAY_IN_MILLIS).toInt()
        var remaining = safeMillis % DAY_IN_MILLIS

        val hours = (remaining / HOUR_IN_MILLIS).toInt()
        remaining %= HOUR_IN_MILLIS

        val minutes = (remaining / MINUTE_IN_MILLIS).toInt()
        remaining %= MINUTE_IN_MILLIS

        val seconds = (remaining / SECOND_IN_MILLIS).toInt()

        return TimeParts(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds
        )
    }

    private data class TimeParts(
        val days: Int,
        val hours: Int,
        val minutes: Int,
        val seconds: Int
    )
}