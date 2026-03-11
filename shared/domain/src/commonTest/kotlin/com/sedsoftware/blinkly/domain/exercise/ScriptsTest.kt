package com.sedsoftware.blinkly.domain.exercise

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.exercise.dsl.CompleteBlockNode
import com.sedsoftware.blinkly.domain.exercise.dsl.CompleteExerciseNode
import com.sedsoftware.blinkly.domain.exercise.dsl.MovementNode
import com.sedsoftware.blinkly.domain.exercise.dsl.TickNode
import com.sedsoftware.blinkly.domain.exercise.scripts.blinkScript
import com.sedsoftware.blinkly.domain.exercise.scripts.clockRollScript
import com.sedsoftware.blinkly.domain.exercise.scripts.diagonalScript
import com.sedsoftware.blinkly.domain.exercise.scripts.figureEightScript
import com.sedsoftware.blinkly.domain.exercise.scripts.nearFarScript
import com.sedsoftware.blinkly.domain.exercise.scripts.palmingScript
import com.sedsoftware.blinkly.domain.exercise.scripts.twentyScript
import com.sedsoftware.blinkly.domain.fakes.FakeData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ScriptsTest : BaseDomainTest() {

    @Test
    fun `when blink script created then correct number of movements`() = runTest(testScheduler) {
        // given
        // when
        val script = blinkScript(settings)
        val movementCount = script.nodes.count { it is MovementNode }

        // then
        assertThat(movementCount).isEqualTo(FakeData.BLINK_COUNT)
        assertThat(script.nodes.last() is CompleteExerciseNode).isTrue()
    }

    @Test
    fun `when palming script created then correct ticks generated`() = runTest(testScheduler) {
        // given
        // when
        val script = palmingScript(settings)
        val ticks = script.nodes.filterIsInstance<TickNode>()

        // then
        assertThat(ticks.size).isEqualTo(FakeData.PALMING_DURATION)
        assertThat(script.nodes.last() is CompleteBlockNode).isTrue()
    }

    @Test
    fun `when clock roll script created then correct movements and completion`() = runTest(testScheduler) {
        // given
        // when
        val script = clockRollScript(settings)
        val movements = script.nodes.filterIsInstance<MovementNode>()
        val expectedMovements = FakeData.CLOCK_COUNT * 2

        // then
        assertThat(movements.size).isEqualTo(expectedMovements)
        assertThat(script.nodes.last() is CompleteExerciseNode).isTrue()
    }

    @Test
    fun `when diagonal script created then correct movements and block completion`() = runTest(testScheduler) {
        // given
        // when
        val script = diagonalScript(settings)
        val movements = script.nodes.filterIsInstance<MovementNode>()
        val expectedMovements = FakeData.DIAGONAL_COUNT * 4

        // then
        assertThat(movements.size).isEqualTo(expectedMovements)
        assertThat(script.nodes.last() is CompleteBlockNode).isTrue()
    }

    @Test
    fun `when figure eight script created then correct movements and completion`() = runTest(testScheduler) {
        // given
        // when
        val script = figureEightScript(settings)
        val movements = script.nodes.filterIsInstance<MovementNode>()
        val expectedMovements = FakeData.FIGURE_EIGHT_COUNT * 2

        // then
        assertThat(movements.size).isEqualTo(expectedMovements)
        assertThat(script.nodes.last() is CompleteExerciseNode).isTrue()
    }

    @Test
    fun `when near far script created then correct movements and completion`() = runTest(testScheduler) {
        // given
        // when
        val script = nearFarScript(settings)
        val movements = script.nodes.filterIsInstance<MovementNode>()
        val expectedMovements = FakeData.NEAR_FAR_COUNT * 2

        // then
        assertThat(movements.size).isEqualTo(expectedMovements)
        assertThat(script.nodes.last() is CompleteExerciseNode).isTrue()
    }

    @Test
    fun `when twenty script created then correct ticks and block completion`() = runTest(testScheduler) {
        // given
        // when
        val script = twentyScript()
        val ticks = script.nodes.filterIsInstance<TickNode>()

        // then
        assertThat(ticks.size).isEqualTo(20)
        assertThat(script.nodes.last() is CompleteBlockNode).isTrue()
    }
}
