package com.sedsoftware.blinkly.component.garden.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.garden.store.GardenStore.Intent
import com.sedsoftware.blinkly.component.garden.store.GardenStore.Label
import com.sedsoftware.blinkly.component.garden.store.GardenStore.State
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeGarden
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType

internal interface GardenStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class TreeClicked(val type: TreeType) : Intent
        data object TreeDetailsDismissed : Intent
    }

    data class State(
        val garden: TreeGarden = TreeGarden(
            currentTree = Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f),
            grownTrees = emptyList(),
            totalTrees = TreeType.entries.size,
            nextTreeType = TreeType.FRAXINUS_EXCELSIOR,
            daysToNextTree = DEFAULT_DAYS_TO_NEXT_TREE,
        ),
        val selectedType: TreeType? = null,
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
    }

    private companion object {
        const val DEFAULT_DAYS_TO_NEXT_TREE = 28
    }
}
