package com.sedsoftware.blinkly.domain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.extension.asLocalDate
import com.sedsoftware.blinkly.domain.impl.HighlightsProviderImpl
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import dev.mokkery.answering.returns
import dev.mokkery.every
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class HighlightsProviderTest : BaseDomainTest() {

    lateinit var provider: HighlightsProvider

    @BeforeTest
    fun setup() {
        provider = HighlightsProviderImpl(settings, timeUtils, testDispatchers)
    }

    @Test
    fun `when first launch then provider should return random fact or tip`() = runTest(testScheduler) {
        // given
        every { timeUtils.now() } returns now
        // when
        val highlight = provider.get()
        val index = when (highlight) {
            is HighlightOfTheDay.Tip -> highlight.index
            is HighlightOfTheDay.Fact -> highlight.index
        }
        // then
        assertThat(highlight is HighlightOfTheDay.Tip || highlight is HighlightOfTheDay.Fact).isTrue()
        assertThat(settings.currentHighlightDate).isEqualTo(now.asLocalDate())
        assertThat(settings.displayedHighlights.size).isEqualTo(1)
        assertThat(settings.displayedHighlights).contains(index)
    }

    @Test
    fun `when provider returned a highlight today then should return the same for all today calls`() = runTest(testScheduler) {
        // given
        every { timeUtils.now() } returns now
        // when
        val highlight1 = provider.get()
        val highlight2 = provider.get()
        // then
        assertThat(highlight1).isEqualTo(highlight2)
    }

    @Test
    fun `when provider returned a highlight yesterday then should return a new one today`() = runTest(testScheduler) {
        // given
        every { timeUtils.now() } returns yesterday
        // when
        val indexYesterday = when (val highlightYesterday = provider.get()) {
            is HighlightOfTheDay.Tip -> highlightYesterday.index
            is HighlightOfTheDay.Fact -> highlightYesterday.index
        }
        // then
        assertThat(settings.currentHighlightDate).isEqualTo(yesterday.asLocalDate())
        assertThat(settings.displayedHighlights.size).isEqualTo(1)

        // given
        every { timeUtils.now() } returns now
        // when
        val indexToday = when (val highlightToday = provider.get()) {
            is HighlightOfTheDay.Tip -> highlightToday.index
            is HighlightOfTheDay.Fact -> highlightToday.index
        }
        // then
        assertThat(settings.currentHighlightDate).isEqualTo(now.asLocalDate())
        assertThat(settings.displayedHighlights.size).isEqualTo(2)
        assertThat(settings.displayedHighlights).contains(indexYesterday)
        assertThat(settings.displayedHighlights).contains(indexToday)
    }

    @Test
    fun `when provider returns a highlight then should not repeat for all consecutive calls`() = runTest(testScheduler) {
        // given
        val highlights = mutableListOf<HighlightOfTheDay>()
        val expectedSize = 50
        // when
        repeat(expectedSize) {
            val highlight = provider.forceNextHighlight()
            highlights.add(highlight)
        }
        // then
        assertThat(highlights.size).isEqualTo(expectedSize)
    }

    @Test
    fun `when reset requested then highlights sequence restarts again`() = runTest(testScheduler) {
        // given
        every { timeUtils.now() } returns yesterday
        val indexYesterday = when (val highlightYesterday = provider.get()) {
            is HighlightOfTheDay.Tip -> highlightYesterday.index
            is HighlightOfTheDay.Fact -> highlightYesterday.index
        }
        assertThat(settings.currentHighlightDate).isEqualTo(yesterday.asLocalDate())
        assertThat(settings.displayedHighlights.size).isEqualTo(1)

        every { timeUtils.now() } returns now
        val indexToday = when (val highlightToday = provider.get()) {
            is HighlightOfTheDay.Tip -> highlightToday.index
            is HighlightOfTheDay.Fact -> highlightToday.index
        }

        assertThat(settings.currentHighlightDate).isEqualTo(now.asLocalDate())
        assertThat(settings.displayedHighlights.size).isEqualTo(2)
        assertThat(settings.displayedHighlights).contains(indexYesterday)
        assertThat(settings.displayedHighlights).contains(indexToday)

        // when
        provider.reset()
        // then
        assertThat(settings.currentHighlightDate).isEqualTo(null)
        assertThat(settings.displayedHighlights).isEmpty()

        // when
        val newHighlightIndex = when (val highlight = provider.get()) {
            is HighlightOfTheDay.Tip -> highlight.index
            is HighlightOfTheDay.Fact -> highlight.index
        }

        // then
        assertThat(settings.currentHighlightDate).isEqualTo(now.asLocalDate())
        assertThat(settings.displayedHighlights.size).isEqualTo(1)
        assertThat(settings.displayedHighlights).contains(newHighlightIndex)
    }

    @Test
    fun `when showCount requested then returned correct value`() = runTest(testScheduler) {
        // given
        every { timeUtils.now() } returns yesterday
        val highlightYesterday = provider.get()
        assertThat(settings.currentHighlightDate).isEqualTo(yesterday.asLocalDate())

        every { timeUtils.now() } returns now
        val highlightToday = provider.get()
        assertThat(settings.currentHighlightDate).isEqualTo(now.asLocalDate())

        // when
        val count = provider.getShownCount()
        // then
        assertThat(count).isEqualTo(2)
    }
}
