package com.sedsoftware.blinkly.domain.impl

import com.sedsoftware.blinkly.domain.BlinklyHighlightsProvider
import com.sedsoftware.blinkly.domain.extension.asLocalDate
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class BlinklyHighlightsProviderImpl(
    private val settings: BlinklySettings,
    private val timeUtils: BlinklyTimeUtils,
    private val dispatchers: BlinklyDispatchers,
) : BlinklyHighlightsProvider {

    override suspend fun get(): HighlightOfTheDay =
        withContext(dispatchers.io) {
            val today = timeUtils.now().asLocalDate(timeUtils.timeZone())

            if (shouldUpdateHighlight(today)) {
                generateAndSaveNewHighlight(today)
            } else {
                val lastIndex = settings.displayedHighlights.lastOrNull() ?: 1
                indexToHighlight(lastIndex)
            }
        }

    private fun shouldUpdateHighlight(today: LocalDate): Boolean {
        val savedDate = settings.currentHighlightDate
        return savedDate == null || savedDate < today
    }

    private fun generateAndSaveNewHighlight(today: LocalDate): HighlightOfTheDay {
        val usedIndices = settings.displayedHighlights.toSet()

        val availableIndices = if (usedIndices.size >= TOTAL_HIGHLIGHTS) {
            emptyList()
        } else {
            (1..TOTAL_HIGHLIGHTS).filter { it !in usedIndices }
        }

        val chosenIndex = if (availableIndices.isNotEmpty()) {
            availableIndices.random()
        } else {
            (1..TOTAL_HIGHLIGHTS).random()
        }

        val newList = settings.displayedHighlights + chosenIndex
        settings.displayedHighlights = newList
        settings.currentHighlightDate = today

        return indexToHighlight(chosenIndex)
    }

    private fun indexToHighlight(index: Int): HighlightOfTheDay =
        when (index) {
            in 1..TOTAL_TIPS -> HighlightOfTheDay.Tip(index)

            in (TOTAL_TIPS + 1)..TOTAL_HIGHLIGHTS -> {
                HighlightOfTheDay.Fact(index)
            }

            else -> {
                // fallback
                HighlightOfTheDay.Tip(1)
            }
        }


    override suspend fun forceNextHighlight(): HighlightOfTheDay =
        withContext(dispatchers.io) {
            val today = timeUtils.now().asLocalDate(timeUtils.timeZone())
            generateAndSaveNewHighlight(today)
        }

    override suspend fun reset() {
        withContext(dispatchers.io) {
            settings.displayedHighlights = emptyList()
            settings.currentHighlightDate = null
        }
    }

    override suspend fun getShownCount(): Int = withContext(dispatchers.io) {
        settings.displayedHighlights.toSet().size
    }

    private companion object {
        private const val TOTAL_TIPS = 30
        private const val TOTAL_FACTS = 20
        private const val TOTAL_HIGHLIGHTS = TOTAL_TIPS + TOTAL_FACTS
    }
}
