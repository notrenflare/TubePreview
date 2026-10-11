package com.renflare.tubepreview.data

import kotlin.random.Random

data class FeedShort(val id: String, val title: String, val thumbnailUrl: String)

object MockShortFeed {
    private val samples = listOf(
        "This camera fits in my pocket" to "1516035069371-29a1b244cc32",
        "My favorite five-minute breakfast" to "1490645935967-10de6ba17061",
        "I could listen to this keyboard all day" to "1587829741301-dc798b83add3",
        "The city after everyone goes home" to "1480714378408-67cf0d13bc1b",
        "A little coffee ritual before work" to "1442512595331-e89e73853f31",
        "Making this small apartment feel like home" to "1522708323590-d24dbb6b0267",
        "The best seat on the morning train" to "1474487548417-781cb71495f3",
        "This view made the whole hike worth it" to "1464822759023-fed622ff2c3b",
        "Thirty seconds of ocean sounds" to "1518837695005-2083093ee35b",
        "The bookstore I could spend all day in" to "1507842217343-583bb7270b66",
        "My easiest weeknight pasta" to "1551183053-bf91a1d81141",
        "One more game before bed" to "1493711662062-fa541adb3fc8"
    ).mapIndexed { index, (title, photo) ->
        FeedShort("short_$index", title, MockFeedProvider.photoUrl(photo, 480, 720))
    }

    fun initial(): List<FeedShort> = samples.take(4)

    fun refreshed(previous: List<FeedShort>, random: Random): List<FeedShort> {
        val previousIds = previous.mapTo(mutableSetOf()) { it.id }
        return samples.filter { it.id !in previousIds }.shuffled(random).take(4)
    }
}
