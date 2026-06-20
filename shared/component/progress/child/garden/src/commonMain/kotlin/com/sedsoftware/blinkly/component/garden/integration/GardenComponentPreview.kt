package com.sedsoftware.blinkly.component.garden.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.garden.GardenComponent
import com.sedsoftware.blinkly.component.garden.GardenComponent.Model
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType

class GardenComponentPreview(
    currentTree: Tree = Tree(TreeStage.GROWING, TreeType.ADANSONIA, PREVIEW_CURRENT_TREE_PROGRESS),
    grownTrees: List<Tree> = previewGrownTrees(),
    selectedTree: Tree? = null,
) : GardenComponent {

    override val model: Value<Model> = MutableValue(
        Model(
            currentTree = currentTree,
            grownTrees = grownTrees,
            grownTreesCount = grownTrees.size,
            totalTrees = TreeType.entries.size,
            nextTreeType = TreeType.entries.getOrNull(grownTrees.size),
            daysToNextTree = PREVIEW_DAYS_TO_NEXT_TREE,
            selectedTree = selectedTree,
        )
    )

    override fun onBackClick() = Unit
    override fun onTreeClick(type: TreeType) = Unit
    override fun onTreeDetailsDismiss() = Unit
}

private fun previewGrownTrees(): List<Tree> =
    listOf(
        Tree(TreeStage.MAGNIFICENT, TreeType.FRAXINUS_EXCELSIOR, FULL_TREE_PROGRESS),
        Tree(TreeStage.MAGNIFICENT, TreeType.GINKGO_BILOBA, FULL_TREE_PROGRESS),
        Tree(TreeStage.MAGNIFICENT, TreeType.SALIX_BABYLONICA, FULL_TREE_PROGRESS),
        Tree(TreeStage.MAGNIFICENT, TreeType.PINUS, FULL_TREE_PROGRESS),
    )

private const val PREVIEW_CURRENT_TREE_PROGRESS = 62f
private const val PREVIEW_DAYS_TO_NEXT_TREE = 9
private const val FULL_TREE_PROGRESS = 100f
