package com.sedsoftware.blinkly.domain.model

enum class TreeStage(val threshold: Float) {
    TINY(1f),
    SMALL(2f),
    YOUNG(3f),
    GROWING(4f),
    STRONG(5f),
    MATURE(6f),
    MAGNIFICENT(7f),
}
