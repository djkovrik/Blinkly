package com.sedsoftware.blinkly.component.garden

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.garden.integration.GardenComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeGarden
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GardenComponentTest : ComponentTest<GardenComponent>() {

    private val gardenFlow: MutableStateFlow<TreeGarden> = MutableStateFlow(emptyGarden())
    private val treeFlow: MutableStateFlow<Tree> = MutableStateFlow(DEFAULT_TREE)

    @Test
    fun `when garden emitted then model contains current tree grown trees and stats`() = runTest(testScheduler) {
        // given
        val currentTree = Tree(TreeStage.GROWING, TreeType.PINUS, 60f)
        val grownTrees = listOf(
            Tree(TreeStage.MAGNIFICENT, TreeType.FRAXINUS_EXCELSIOR, 100f),
            Tree(TreeStage.MAGNIFICENT, TreeType.GINKGO_BILOBA, 100f),
        )
        gardenFlow.value = TreeGarden(
            currentTree = currentTree,
            grownTrees = grownTrees,
            totalTrees = TreeType.entries.size,
            nextTreeType = TreeType.SALIX_BABYLONICA,
            daysToNextTree = 11,
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        val model = component.model.value
        assertThat(model.currentTree).isEqualTo(currentTree)
        assertThat(model.grownTrees).isEqualTo(grownTrees)
        assertThat(model.grownTreesCount).isEqualTo(2)
        assertThat(model.totalTrees).isEqualTo(TreeType.entries.size)
        assertThat(model.nextTreeType).isEqualTo(TreeType.SALIX_BABYLONICA)
        assertThat(model.daysToNextTree).isEqualTo(11)
        assertThat(model.selectedTree).isNull()
    }

    @Test
    fun `when grown tree clicked then details are shown and can be dismissed`() = runTest(testScheduler) {
        // given
        val grownTree = Tree(TreeStage.MAGNIFICENT, TreeType.FRAXINUS_EXCELSIOR, 100f)
        gardenFlow.value = emptyGarden(grownTrees = listOf(grownTree))
        testScheduler.advanceUntilIdle()

        // when
        component.onTreeClick(TreeType.FRAXINUS_EXCELSIOR)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.selectedTree).isEqualTo(grownTree)

        // when
        component.onTreeDetailsDismiss()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.selectedTree).isNull()
    }

    @Test
    fun `when not grown tree clicked then details are not shown`() = runTest(testScheduler) {
        // given
        gardenFlow.value = emptyGarden(
            grownTrees = listOf(Tree(TreeStage.MAGNIFICENT, TreeType.FRAXINUS_EXCELSIOR, 100f))
        )
        testScheduler.advanceUntilIdle()

        // when
        component.onTreeClick(TreeType.GINKGO_BILOBA)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.selectedTree).isNull()
    }

    @Test
    fun `when selected tree disappears then details are hidden`() = runTest(testScheduler) {
        // given
        val grownTree = Tree(TreeStage.MAGNIFICENT, TreeType.FRAXINUS_EXCELSIOR, 100f)
        gardenFlow.value = emptyGarden(grownTrees = listOf(grownTree))
        testScheduler.advanceUntilIdle()
        component.onTreeClick(TreeType.FRAXINUS_EXCELSIOR)
        testScheduler.advanceUntilIdle()

        // when
        gardenFlow.value = emptyGarden(grownTrees = emptyList())
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.selectedTree).isNull()
    }

    @Test
    fun `when back clicked then component emits back output`() = runTest(testScheduler) {
        // when
        component.onBackClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Common.BackPressed)
    }

    @Test
    fun `when garden watcher fails then component publishes error output`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("garden failed")
        val testComponent = createComponent(
            watcher = object : BlinklyTreeProgressWatcher {
                override val tree: Flow<Tree> = treeFlow
                override val garden: Flow<TreeGarden> = flow { throw exception }
            }
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(testComponent).isInstanceOf(GardenComponent::class)
        assertThat(
            componentOutput.filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable.message == exception.message }
        ).isTrue()
    }

    override fun createComponent(): GardenComponent =
        createComponent(
            watcher = object : BlinklyTreeProgressWatcher {
                override val tree: Flow<Tree> = treeFlow
                override val garden: Flow<TreeGarden> = gardenFlow
            }
        )

    private fun createComponent(
        watcher: BlinklyTreeProgressWatcher,
    ): GardenComponent =
        GardenComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            treeProgressWatcher = watcher,
            gardenOutput = { componentOutput.add(it) },
        )

    private fun emptyGarden(
        grownTrees: List<Tree> = emptyList(),
    ): TreeGarden =
        TreeGarden(
            currentTree = DEFAULT_TREE,
            grownTrees = grownTrees,
            totalTrees = TreeType.entries.size,
            nextTreeType = TreeType.FRAXINUS_EXCELSIOR,
            daysToNextTree = 28,
        )

    private companion object {
        val DEFAULT_TREE: Tree = Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f)
    }
}
