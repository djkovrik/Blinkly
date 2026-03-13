package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay

interface HighlightsProvider {
    suspend fun get(): HighlightOfTheDay
    suspend fun forceNextHighlight(): HighlightOfTheDay
    suspend fun reset()
    suspend fun getShownCount(): Int
}
