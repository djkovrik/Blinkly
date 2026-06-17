package com.sedsoftware.blinkly.component.garden

import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeType

interface GardenComponent {

    val model: Value<Model>

    fun onBackClick()
    fun onTreeClick(type: TreeType)
    fun onTreeDetailsDismiss()

    data class Model(
        val currentTree: Tree,
        val grownTrees: List<Tree>,
        val grownTreesCount: Int,
        val totalTrees: Int,
        val nextTreeType: TreeType?,
        val daysToNextTree: Int?,
        val selectedTree: Tree?,
    )
}
