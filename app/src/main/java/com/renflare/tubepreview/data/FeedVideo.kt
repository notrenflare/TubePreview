package com.renflare.tubepreview.data

data class FeedVideo(
    val id: String,
    val title: String,
    val channelName: String,
    val channelAvatarUrl: String = "",
    val previewThumbnailUrl: String = "",
    val views: String,
    val duration: String,
    val uploadTime: String,
    val isUserPreview: Boolean = false,
    val gradientIndex: Int = 0,
    val avatarColorHex: String = "",
    val avatarLetter: String = "",
    val thumbnailLabel: String = "",
    val avatarCrop: AvatarCrop = AvatarCrop()
)

data class FakeCreator(
    val channelName: String,
    val avatarColorHex: String,
    val avatarPhoto: String,
    val videos: List<FakeVideoTemplate>
)

data class FakeVideoTemplate(
    val title: String,
    val duration: String,
    val views: String,
    val uploadTime: String,
    val photo: String,
    val thumbnailLabel: String
)

// Curated stock photos, paired with fictional creators rather than impersonating real channels.
// Source and license: https://unsplash.com/license. Coil caches images; graphics remain offline fallbacks.
object MockFeedProvider {
    fun photoUrl(photo: String, width: Int = 960, height: Int = 540): String =
        "https://images.unsplash.com/photo-$photo?auto=format&fit=crop&w=$width&h=$height&q=80"

    val creators = listOf(
        FakeCreator("Alex Chen", "#E55545", "1519389950473-47ba0277781c", listOf(
            FakeVideoTemplate("I Tried Replacing My Laptop With My Phone", "12:42", "1.2M views", "2 hours ago", "1511707171634-5f897ff02aa9", "LAPTOP REPLACEMENT?"),
            FakeVideoTemplate("The Desk Setup I Actually Use Every Day", "15:05", "840K views", "1 week ago", "1496181133206-80ce9b88a853", "MY DAILY SETUP"),
            FakeVideoTemplate("I Bought the Cheapest Camera I Could Find", "11:28", "410K views", "3 days ago", "1516035069371-29a1b244cc32", "HOW BAD IS IT?")
        )),
        FakeCreator("after hours", "#A29BFE", "1518837695005-2083093ee35b", listOf(
            FakeVideoTemplate("rain on the window · late night jazz", "58:45", "5.1M views", "2 weeks ago", "1519608487953-e999c86e7455", "after hours"),
            FakeVideoTemplate("a quiet morning, nowhere to be", "44:20", "2.4M views", "1 month ago", "1447933601403-0c6688de566e", "slow mornings"),
            FakeVideoTemplate("one more chapter · music for reading", "52:16", "920K views", "5 days ago", "1507842217343-583bb7270b66", "one more chapter")
        )),
        FakeCreator("Maya's Kitchen", "#FF7675", "1556909114-f6e7ad7d3136", listOf(
            FakeVideoTemplate("The Pasta I Make When I Don't Feel Like Cooking", "10:15", "340K views", "1 day ago", "1551183053-bf91a1d81141", "15 MINUTE PASTA"),
            FakeVideoTemplate("Why Your Homemade Pizza Doesn't Taste Right", "16:40", "4.2M views", "6 days ago", "1513104890138-7c749659a591", "FIX YOUR PIZZA"),
            FakeVideoTemplate("The Lunch I Make When the Fridge Is Almost Empty", "09:34", "780K views", "2 weeks ago", "1547592180-85f173990554", "FRIDGE CLEAN-OUT")
        )),
        FakeCreator("Miles with Jules", "#00CEC9", "1464822759023-fed622ff2c3b", listOf(
            FakeVideoTemplate("48 Hours in Tokyo With No Plan", "22:18", "1.8M views", "4 days ago", "1540959733332-eab4deabeeaf", "LOST IN TOKYO"),
            FakeVideoTemplate("I Took the Train Everyone Told Me to Skip", "25:10", "950K views", "3 weeks ago", "1474487548417-781cb71495f3", "THE LONG WAY HOME"),
            FakeVideoTemplate("This Hike Was Worth the 4 AM Alarm", "18:37", "620K views", "8 days ago", "1464822759023-fed622ff2c3b", "WORTH IT")
        )),
        FakeCreator("Sam Builds", "#6C5CE7", "1515879218367-8466d910aaa4", listOf(
            FakeVideoTemplate("I Built an App to Fix My Most Annoying Habit", "09:54", "75K views", "5 hours ago", "1515879218367-8466d910aaa4", "I BUILT THIS"),
            FakeVideoTemplate("My Side Project Got Its First Paying Customer", "13:12", "290K views", "12 days ago", "1519389950473-47ba0277781c", "THE FIRST $10"),
            FakeVideoTemplate("I Deleted Half My Code. The App Got Better.", "14:08", "180K views", "1 week ago", "1498050108023-c5249f4df085", "LESS CODE. BETTER APP.")
        )),
        FakeCreator("Small Questions", "#0984E3", "1446776811953-b23d57bd21aa", listOf(
            FakeVideoTemplate("Why Is Space So Quiet?", "11:05", "8.9M views", "2 months ago", "1446776811953-b23d57bd21aa", "CAN YOU HEAR SPACE?"),
            FakeVideoTemplate("The Ocean Is Deeper Than You Think", "14:38", "12M views", "4 months ago", "1518837695005-2083093ee35b", "WHAT'S DOWN THERE?"),
            FakeVideoTemplate("Why Do We Forget What We Just Walked In For?", "10:24", "2.3M views", "3 weeks ago", "1455390582262-044cdead277a", "WAIT... WHY AM I HERE?")
        )),
        FakeCreator("Oliver Plays", "#E84393", "1493711662062-fa541adb3fc8", listOf(
            FakeVideoTemplate("I Spent a Week Learning One Boss Fight", "18:50", "480K views", "5 days ago", "1493711662062-fa541adb3fc8", "ONE MORE TRY"),
            FakeVideoTemplate("The Speedrun Record Nobody Thought Was Possible", "41:12", "2.1M views", "2 weeks ago", "1511512578047-dfb367046420", "A PERFECT RUN"),
            FakeVideoTemplate("I Went Back to the Game I Grew Up With", "24:06", "720K views", "1 month ago", "1486572788966-cfd3df1f5b42", "STILL HOLDS UP?")
        )),
        FakeCreator("Nora Ellis", "#636E72", "1449247709967-d4461a6a6103", listOf(
            FakeVideoTemplate("My Tiny Apartment Finally Feels Like Home", "13:50", "1.1M views", "3 days ago", "1522708323590-d24dbb6b0267", "350 SQ FT"),
            FakeVideoTemplate("A Weekend Reset That Doesn't Take All Weekend", "11:02", "670K views", "10 days ago", "1449247709967-d4461a6a6103", "A LITTLE RESET"),
            FakeVideoTemplate("Everything I Stopped Buying", "12:46", "530K views", "2 weeks ago", "1484154218962-a197022b5858", "LESS STUFF")
        )),
        FakeCreator("James, Probably", "#2ECC71", "1442512595331-e89e73853f31", listOf(
            FakeVideoTemplate("I Got Way Too Into Making Coffee at Home", "15:20", "150K views", "yesterday", "1442512595331-e89e73853f31", "DOWN THE RABBIT HOLE"),
            FakeVideoTemplate("Can You Actually Hear the Difference?", "19:42", "3.1M views", "1 month ago", "1511379938547-c1f69419868d", "$20 VS $200"),
            FakeVideoTemplate("I Tried Doing Nothing for an Entire Day", "11:30", "890K views", "1 week ago", "1470252649378-9c29740c9fa8", "NO PLANS")
        )),
        FakeCreator("Leah Park", "#E17055", "1454165804606-c3d57bc86b40", listOf(
            FakeVideoTemplate("What My Small Business Actually Makes in a Month", "14:15", "88K views", "2 days ago", "1454165804606-c3d57bc86b40", "THE REAL NUMBERS"),
            FakeVideoTemplate("I Took a Week Off. Nothing Fell Apart.", "11:32", "240K views", "1 week ago", "1476514525535-07fb3b4ae5f1", "OUT OF OFFICE"),
            FakeVideoTemplate("The Client Project I Should Have Said No To", "16:07", "190K views", "3 weeks ago", "1497366754035-f200968a6e72", "I LEARNED THE HARD WAY")
        ))
    )

    fun getPlaceholderVideos(): List<FeedVideo> =
        (0 until creators.maxOf { it.videos.size }).flatMap { templateIndex ->
            creators.mapIndexedNotNull { creatorIndex, creator ->
                creator.videos.getOrNull(templateIndex)?.let { template ->
                    FeedVideo(
                        id = "mock_${creatorIndex}_$templateIndex",
                        title = template.title,
                        channelName = creator.channelName,
                        channelAvatarUrl = photoUrl(creator.avatarPhoto, 128, 128),
                        previewThumbnailUrl = photoUrl(template.photo),
                        views = template.views,
                        duration = template.duration,
                        uploadTime = template.uploadTime,
                        gradientIndex = creatorIndex % 7,
                        avatarColorHex = creator.avatarColorHex,
                        avatarLetter = creator.channelName.take(1).uppercase(),
                        thumbnailLabel = template.thumbnailLabel
                    )
                }
            }
        }
}
