package com.sedsoftware.blinkly.compose.ui.sync

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class BlinklySyncContentTest {

    @Test
    fun `sync date is formatted in user time zone`() {
        val syncedAt = Instant.parse("2026-07-25T19:13:42Z")

        val formatted = syncedAt.asSyncDate(timeZone = TimeZone.of("Europe/Moscow"))

        assertEquals("2026-07-25 22:13:42", formatted)
    }
}
