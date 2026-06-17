package com.sedsoftware.blinkly.component.garden.integration

import com.sedsoftware.blinkly.component.garden.GardenComponent.Model
import com.sedsoftware.blinkly.component.garden.store.GardenStore.State

internal val stateToModel: (State) -> Model = { state ->
    Model(
        currentTree = state.garden.currentTree,
        grownTrees = state.garden.grownTrees,
        grownTreesCount = state.garden.grownTrees.size,
        totalTrees = state.garden.totalTrees,
        nextTreeType = state.garden.nextTreeType,
        daysToNextTree = state.garden.daysToNextTree,
        selectedTree = state.garden.grownTrees.firstOrNull { it.type == state.selectedType },
    )
}
