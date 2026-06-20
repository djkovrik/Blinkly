package com.sedsoftware.blinkly.domain.model

data class TreeGarden(
    val currentTree: Tree,
    val grownTrees: List<Tree>,
    val totalTrees: Int,
    val nextTreeType: TreeType?,
    val daysToNextTree: Int?,
)
