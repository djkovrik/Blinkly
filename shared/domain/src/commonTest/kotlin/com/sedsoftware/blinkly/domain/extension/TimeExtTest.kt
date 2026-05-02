package com.sedsoftware.blinkly.domain.extension

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class TimeExtTest {

    @Test
    fun `toHumanReadableString should format time with leading zeroes`() {
        // given
        val time = LocalTime(hour = 1, minute = 2)

        // when
        val result = time.toHumanReadableString()

        // then
        assertThat(result).isEqualTo("01:02")
    }

    @Test
    fun `toHumanReadableString should format time without leading zeroes`() {
        // given
        val time = LocalTime(hour = 12, minute = 34)

        // when
        val result = time.toHumanReadableString()

        // then
        assertThat(result).isEqualTo("12:34")
    }

    @Test
    fun `toHumanReadableString should ignore seconds and nanoseconds`() {
        // given
        val time = LocalTime(hour = 23, minute = 59, second = 58, nanosecond = 123)

        // when
        val result = time.toHumanReadableString()

        // then
        assertThat(result).isEqualTo("23:59")
    }
}
