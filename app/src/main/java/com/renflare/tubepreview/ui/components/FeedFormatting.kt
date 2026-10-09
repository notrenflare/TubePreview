package com.renflare.tubepreview.ui.components

import java.math.BigDecimal
import java.math.RoundingMode

private val viewCount = Regex(
    """^(\d[\d,]*(?:\.\d+)?)\s*(k|m|b|thousand|million|billion)?(?:\s+views?)?$""",
    RegexOption.IGNORE_CASE
)

/** Format numeric counts for the feed without changing saved preview settings. */
internal fun compactViews(value: String): String {
    val input = value.trim()
    val match = viewCount.matchEntire(input) ?: return input
    val number = match.groupValues[1].replace(",", "").toBigDecimalOrNull() ?: return input
    val multiplier = when (match.groupValues[2].lowercase()) {
        "k", "thousand" -> BigDecimal("1000")
        "m", "million" -> BigDecimal("1000000")
        "b", "billion" -> BigDecimal("1000000000")
        else -> BigDecimal.ONE
    }
    val total = number * multiplier
    val (divisor, suffix) = when {
        total >= BigDecimal("1000000000") -> BigDecimal("1000000000") to "B"
        total >= BigDecimal("1000000") -> BigDecimal("1000000") to "M"
        total >= BigDecimal("1000") -> BigDecimal("1000") to "K"
        else -> BigDecimal.ONE to ""
    }
    return total.divide(divisor, if (suffix.isEmpty()) 0 else 1, RoundingMode.DOWN)
        .stripTrailingZeros().toPlainString() + suffix
}

private val elapsedTime = Regex(
    """^(\d+)\s*(seconds?|secs?|s|minutes?|mins?|m|hours?|hrs?|h|days?|d|weeks?|wks?|w|months?|mos?|mo|years?|yrs?|y)\s+ago$""",
    RegexOption.IGNORE_CASE
)

internal fun compactPublishTime(value: String): String {
    val input = value.trim()
    if (input.equals("yesterday", ignoreCase = true)) return "1d ago"
    val match = elapsedTime.matchEntire(input) ?: return input
    val unit = match.groupValues[2].lowercase()
    val abbreviated = when {
        unit.startsWith("mo") -> "mo"
        unit.startsWith("s") -> "s"
        unit.startsWith("m") -> "m"
        unit.startsWith("h") -> "h"
        unit.startsWith("d") -> "d"
        unit.startsWith("w") -> "w"
        else -> "y"
    }
    return "${match.groupValues[1]}$abbreviated ago"
}
