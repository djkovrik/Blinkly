package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.model.TreeGarden
import com.sedsoftware.blinkly.domain.model.Tree
import kotlinx.coroutines.flow.Flow

interface BlinklyTreeProgressWatcher {
    val tree: Flow<Tree>
    val garden: Flow<TreeGarden>
}
