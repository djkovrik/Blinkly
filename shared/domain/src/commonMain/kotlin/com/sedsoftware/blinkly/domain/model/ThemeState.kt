package com.sedsoftware.blinkly.domain.model

enum class ThemeState(val index: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromInt(index: Int): ThemeState =
            entries.firstOrNull { it.index == index } ?: SYSTEM
    }
}
