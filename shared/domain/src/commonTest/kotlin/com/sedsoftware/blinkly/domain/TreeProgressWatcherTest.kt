package com.sedsoftware.blinkly.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.fakes.FakeData
import com.sedsoftware.blinkly.domain.impl.TreeProgressWatcherImpl
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TreeProgressWatcherTest : BaseDomainTest() {

    private val database: BlinklyDatabase = mock()

    @Test
    fun `when no data in database then returns FRAXINUS_EXCELSIOR TINY with 0 percent`() = runTest(testScheduler) {
        // given
        every { database.currentCalendar() } returns flowOf(emptyList())
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f))
    }

    @Test
    fun `when one day with single block then TINY 50 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getSingleExerciseCalendar(now)
        every { database.currentCalendar() } returns flowOf(calendar)
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 50f))
    }

    @Test
    fun `when one day with two or more blocks then TINY 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 1)
        every { database.currentCalendar() } returns flowOf(calendar)
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 100f))
    }

    @Test
    fun `when two full days completed then SMALL 50 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 2)
        every { database.currentCalendar() } returns flowOf(calendar)
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.SMALL, TreeType.FRAXINUS_EXCELSIOR, 50f))
    }

    @Test
    fun `when three full days completed today then SMALL 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 3)
        every { database.currentCalendar() } returns flowOf(calendar)
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.SMALL, TreeType.FRAXINUS_EXCELSIOR, 100f))
    }

    @Test
    fun `when three full days completed yesterday then YOUNG 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 3)
        every { database.currentCalendar() } returns flowOf(calendar)
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.YOUNG, TreeType.FRAXINUS_EXCELSIOR, 0f))
    }

    @Test
    fun `when TINY stage completed today then remains current stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 1)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 100f))
    }

    @Test
    fun `when TINY stage completed yesterday then next day shows SMALL stage with 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 1)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.SMALL, TreeType.FRAXINUS_EXCELSIOR, 0f))
    }

    @Test
    fun `when SMALL stage completed today then remains current stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 31)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.SMALL, TreeType.GINKGO_BILOBA, 100f))
    }

    @Test
    fun `when SMALL stage completed yesterday then next day shows YOUNG stage with 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 31)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.YOUNG, TreeType.GINKGO_BILOBA, 0f))
    }

    @Test
    fun `when YOUNG stage completed today then remains current stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 62)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.YOUNG, TreeType.SALIX_BABYLONICA, 100f))
    }

    @Test
    fun `when YOUNG stage completed yesterday then next day shows GROWING stage with 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 62)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.GROWING, TreeType.SALIX_BABYLONICA, 0f))
    }

    @Test
    fun `when GROWING stage completed today then remains current stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 94)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.GROWING, TreeType.PINUS, 100f))
    }

    @Test
    fun `when GROWING stage completed yesterday then next day shows STRONG stage with 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 94)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.STRONG, TreeType.PINUS, 0f))
    }

    @Test
    fun `when STRONG stage completed today then remains current stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 127)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.STRONG, TreeType.ADANSONIA, 100f))
    }

    @Test
    fun `when STRONG stage completed yesterday then next day shows MATURE stage with 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 127)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MATURE, TreeType.ADANSONIA, 0f))
    }

    @Test
    fun `when MATURE stage completed today then remains current stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 161)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MATURE, TreeType.MIMOSA_PUDICA, 100f))
    }

    @Test
    fun `when MATURE stage completed yesterday then next day shows MAGNIFICENT stage with 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 161)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.MIMOSA_PUDICA, 0f))
    }

    @Test
    fun `when full FRAXINUS_EXCELSIOR completed today then remains MAGNIFICENT stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 28)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.FRAXINUS_EXCELSIOR, 100f))
    }

    @Test
    fun `when FRAXINUS_EXCELSIOR completed yesterday then next day shows GINKGO_BILOBA with TINY 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 28)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.GINKGO_BILOBA, 0f))
    }

    @Test
    fun `when full GINKGO_BILOBA completed today then remains MAGNIFICENT stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 56)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.GINKGO_BILOBA, 100f))
    }

    @Test
    fun `when GINKGO_BILOBA completed yesterday then next day shows SALIX_BABYLONICA with TINY 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 56)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.SALIX_BABYLONICA, 0f))
    }

    @Test
    fun `when full SALIX_BABYLONICA completed today then remains MAGNIFICENT stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 84)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.SALIX_BABYLONICA, 100f))
    }

    @Test
    fun `when SALIX_BABYLONICA completed yesterday then next day shows PINUS with TINY 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 84)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.PINUS, 0f))
    }

    @Test
    fun `when full PINUS completed today then remains MAGNIFICENT stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 112)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.PINUS, 100f))
    }

    @Test
    fun `when PINUS completed yesterday then next day shows ADANSONIA with TINY 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 112)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.ADANSONIA, 0f))
    }

    @Test
    fun `when full ADANSONIA completed today then remains MAGNIFICENT stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 140)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.ADANSONIA, 100f))
    }

    @Test
    fun `when ADANSONIA completed yesterday then next day shows MIMOSA_PUDICA with TINY 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 140)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.MIMOSA_PUDICA, 0f))
    }

    @Test
    fun `when full MIMOSA_PUDICA completed today then remains MAGNIFICENT stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 168)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.MIMOSA_PUDICA, 100f))
    }

    @Test
    fun `when MIMOSA_PUDICA completed yesterday then next day shows SEQUOIA_SEMPERVIRENS with TINY 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 168)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.SEQUOIA_SEMPERVIRENS, 0f))
    }

    @Test
    fun `when full SEQUOIA_SEMPERVIRENS completed today then remains MAGNIFICENT stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 196)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.SEQUOIA_SEMPERVIRENS, 100f))
    }

    @Test
    fun `when SEQUOIA_SEMPERVIRENS completed yesterday then next day shows BETULA with TINY 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 196)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.BETULA, 0f))
    }

    @Test
    fun `when full BETULA completed today then remains MAGNIFICENT stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 224)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.BETULA, 100f))
    }

    @Test
    fun `when BETULA completed yesterday then next day shows FICUS_BENJAMINA with TINY 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 224)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.FICUS_BENJAMINA, 0f))
    }

    @Test
    fun `when full FICUS_BENJAMINA completed today then remains MAGNIFICENT stage 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 252)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.FICUS_BENJAMINA, 100f))
    }

    @Test
    fun `when FICUS_BENJAMINA completed yesterday then next day shows QUERCUS_ROBUR with TINY 0 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(yesterday, 252)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.TINY, TreeType.QUERCUS_ROBUR, 0f))
    }

    @Test
    fun `when all trees completed then remains QUERCUS_ROBUR MAGNIFICENT 100 percent`() = runTest(testScheduler) {
        // given
        val calendar = FakeData.getCalendarWithFullDays(now, 280)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        // when
        val result = watcher.tree.first()

        // then
        assertThat(result).isEqualTo(Tree(TreeStage.MAGNIFICENT, TreeType.QUERCUS_ROBUR, 100f))
    }

    @Test
    fun `when progress exceeds full cycle then remains QUERCUS_ROBUR MAGNIFICENT 100 percent`() = runTest(testScheduler) {
        // 281 день полного прогресса (280 + 1 лишний)
        val calendar = FakeData.getCalendarWithFullDays(now, 281)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        val result = watcher.tree.first()

        assertThat(result).isEqualTo(
            Tree(TreeStage.MAGNIFICENT, TreeType.QUERCUS_ROBUR, 100f)
        )
    }

    @Test
    fun `when progress significantly exceeds full cycle then still QUERCUS_ROBUR MAGNIFICENT 100 percent`() = runTest(testScheduler) {
        // 300 дней — намного больше 280
        val calendar = FakeData.getCalendarWithFullDays(now, 300)
        every { database.currentCalendar() } returns flowOf(calendar)
        every { timeUtils.now() } returns now
        val watcher = TreeProgressWatcherImpl(database, timeUtils, testDispatchers)

        val result = watcher.tree.first()

        assertThat(result).isEqualTo(
            Tree(TreeStage.MAGNIFICENT, TreeType.QUERCUS_ROBUR, 100f)
        )
    }
}
