package com.renflare.tubepreview

import com.renflare.tubepreview.ui.components.compactPublishTime
import com.renflare.tubepreview.ui.components.compactViews
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedMetadataTest {
    @Test fun countsAcceptSavedLabelsAndRawNumbers() {
        mapOf(
            "1.2M views" to "1.2M", "75k views" to "75K", "840K" to "840K",
            "5,200,000" to "5.2M", "1234" to "1.2K", "999" to "999",
            "2 million views" to "2M", "3 billion" to "3B",
            "999,999" to "999.9K", "0 views" to "0"
        ).forEach { (input, expected) -> assertEquals(input, expected, compactViews(input)) }
    }

    @Test fun timeUnitsDistinguishMinutesFromMonths() {
        mapOf(
            "2 days ago" to "2d ago", "1 hour ago" to "1h ago",
            "30 minutes ago" to "30m ago", "4 months ago" to "4mo ago",
            "5 seconds ago" to "5s ago", "2 weeks ago" to "2w ago",
            "1 year ago" to "1y ago", "yesterday" to "1d ago",
            "2d ago" to "2d ago", "3mo ago" to "3mo ago"
        ).forEach { (input, expected) -> assertEquals(input, expected, compactPublishTime(input)) }
    }

    @Test fun blankAndCustomLabelsRemainUsable() {
        for (input in listOf("", "No views yet", "LIVE", "-20", "1.2.3M")) {
            assertEquals(input, compactViews(input))
        }
        for (input in listOf("", "Just now", "Premiered today", "LIVE")) {
            assertEquals(input, compactPublishTime(input))
        }
    }
}
