package com.sedsoftware.blinkly.component.home

import assertk.assertThat
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.home.integration.HomeScreenComponentDefault
import com.sedsoftware.blinkly.component.home.model.HomeScreenTab
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyHighlightsProvider
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeGarden
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import com.sedsoftware.blinkly.domain.model.Workout
import dev.mokkery.mock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

class HomeScreenComponentTest : ComponentTest<HomeScreenComponent>() {

    private val settingsMock: BlinklySettings = mock()
    private val fakeSettings = FakeSettings(settingsMock = settingsMock)
    private val timeUtils: BlinklyTimeUtils =
        object : BlinklyTimeUtils {
            override fun now(): Instant = Clock.System.now()
            override fun timeZone(): TimeZone = TimeZone.UTC
        }

    @Test
    fun `when component created then onboarding displayed flag is true`() = runTest(testScheduler) {
        // given
        // when
        // then
        assertThat(fakeSettings.onboardingDisplayed).isTrue()
    }

    @Test
    fun `when tab selected then stack top child updated`() = runTest(testScheduler) {
        // given
        // when
        // then
        assertThat(component.childStack.active.instance is HomeScreenComponent.Child.MainTab).isTrue()
        // when
        component.onTabClick(HomeScreenTab.MAIN)
        // then
        assertThat(component.childStack.active.instance is HomeScreenComponent.Child.MainTab).isTrue()
        // when
        component.onTabClick(HomeScreenTab.TRAINING)
        // then
        assertThat(component.childStack.active.instance is HomeScreenComponent.Child.TrainingsTab).isTrue()
        // when
        component.onTabClick(HomeScreenTab.PROGRESS)
        // then
        assertThat(component.childStack.active.instance is HomeScreenComponent.Child.ProgressTab).isTrue()
        // when
        component.onTabClick(HomeScreenTab.REMINDERS)
        // then
        assertThat(component.childStack.active.instance is HomeScreenComponent.Child.RemindersTab).isTrue()
    }

    @Test
    fun `when main tab opens progress then stack top child updated`() = runTest(testScheduler) {
        // given
        val mainTab = component.childStack.active.instance as HomeScreenComponent.Child.MainTab

        // when
        mainTab.component.onTreeClick()

        // then
        assertThat(component.childStack.active.instance is HomeScreenComponent.Child.ProgressTab).isTrue()
    }

    override fun createComponent(): HomeScreenComponent =
        HomeScreenComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            settings = fakeSettings,
            timeUtils = timeUtils,
            calendarWatcher = object : BlinklyCalendarWatcher {
                override val calendar: Flow<List<Workout>> = flowOf(emptyList())
            },
            highlightsProvider = object : BlinklyHighlightsProvider {
                override suspend fun get(): HighlightOfTheDay = HighlightOfTheDay.Tip(1)
                override suspend fun forceNextHighlight(): HighlightOfTheDay = HighlightOfTheDay.Tip(2)
                override suspend fun reset() = Unit
                override suspend fun getShownCount(): Int = 1
            },
            reminderManager = object : BlinklyReminderManager {
                override fun createdReminders(): Flow<List<Reminder>> = flowOf(emptyList())
                override suspend fun scheduleDaily(time: LocalTime) = Unit
                override suspend fun scheduleWeeklySingle(time: LocalTime, dayOfWeek: DayOfWeek) = Unit
                override suspend fun scheduleWeeklyDayPeriod(
                    from: LocalTime,
                    until: LocalTime,
                    intervalMinutes: Int,
                    days: List<DayOfWeek>,
                ) = Unit
                override suspend fun rescheduleAll() = Unit
                override suspend fun cancel(uuid: String) = Unit
                override suspend fun cancelAll() = Unit
            },
            treeProgressWatcher = object : BlinklyTreeProgressWatcher {
                override val tree: Flow<Tree> = flowOf(Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f))
                override val garden: Flow<TreeGarden> = flowOf(emptyGarden())
            },
            homeScreenOutput = { componentOutput.add(it) },
        )

    private fun emptyGarden(): TreeGarden =
        TreeGarden(
            currentTree = Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f),
            grownTrees = emptyList(),
            totalTrees = TreeType.entries.size,
            nextTreeType = TreeType.FRAXINUS_EXCELSIOR,
            daysToNextTree = 28,
        )
}
