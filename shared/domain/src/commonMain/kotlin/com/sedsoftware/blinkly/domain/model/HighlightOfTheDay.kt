package com.sedsoftware.blinkly.domain.model

sealed class HighlightOfTheDay {
    data class Tip(val index: Int) : HighlightOfTheDay()
    data class Fact(val index: Int) : HighlightOfTheDay()
}
