package com.sedsoftware.blinkly.domain.model

data class Tree(
    val stage: TreeStage,
    val type: TreeType,
    val totalDays: Int,
)
