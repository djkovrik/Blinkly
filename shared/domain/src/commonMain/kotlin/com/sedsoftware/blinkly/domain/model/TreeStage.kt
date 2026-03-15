package com.sedsoftware.blinkly.domain.model

enum class TreeStage(val index: Int, val threshold: Float) {
    TINY(1, 1f),
    SMALL(2, 2f),
    YOUNG(3, 3f),
    GROWING(4, 4f),
    STRONG(5, 5f),
    MATURE(6, 6f),
    MAGNIFICENT(7, 7f);
}
