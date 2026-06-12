package com.sedsoftware.blinkly.component.root

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.component.home.model.HomeScreenTab
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent
import com.sedsoftware.blinkly.component.root.integration.RootComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyAchievementsWatcher
import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.BlinklyExerciseManager
import com.sedsoftware.blinkly.domain.BlinklyHighlightsProvider
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.BlinklyTreeProgressWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ThemeState
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.time.Clock

class RootComponentTest : ComponentTest<RootComponent>() {

    private val alarmManagerMock: BlinklyAlarmManager = mock()
    private val databaseMock: BlinklyDatabase = mock()
    private val notifierMock: BlinklyNotifier = mock()
    private val fakeSettings = FakeSettings()
    private val timeUtilsMock: BlinklyTimeUtils = mock {
        every { now() } returns Clock.System.now()
        every { timeZone() } returns TimeZone.UTC
    }
    private val achievementsWatcherMock: BlinklyAchievementsWatcher = mock()
    private val calendarWatcherMock: BlinklyCalendarWatcher = mock {
        every { calendar } returns emptyFlow()
    }
    private val exerciseManagerMock: BlinklyExerciseManager = mock()
    private val highlightsProviderMock: BlinklyHighlightsProvider = mock {
        everySuspend { get() } returns com.sedsoftware.blinkly.domain.model.HighlightOfTheDay.Tip(1)
    }
    private val reminderManagerMock: BlinklyReminderManager = mock()
    private val treeProgressWatcherMock: BlinklyTreeProgressWatcher = mock {
        every { tree } returns kotlinx.coroutines.flow.emptyFlow()
    }

    @Test
    fun `when component created for the first time then initial child is onboarding`() = runTest(testScheduler) {
        // given
        // when
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.Onboarding).isTrue()
    }

    @Test
    fun `when component created not the first time then initial child is home`() = runTest(testScheduler) {
        // given
        // when
        fakeSettings.onboardingDisplayed = true
        val testComponent = createComponent()
        // then
        assertThat(testComponent.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when Preferences theme changed then root theme state is updated`() = runTest(testScheduler) {
        // given
        fakeSettings.onboardingDisplayed = true
        val testComponent = createComponent()
        val homeScreenChild = testComponent.childStack.active.instance as RootComponent.Child.HomeScreen
        val currentTabChild = homeScreenChild.component.childStack.active.instance as HomeScreenComponent.Child.MainTab

        // when
        currentTabChild.component.onPreferencesClick()
        val preferencesChild = testComponent.childStack.active.instance as RootComponent.Child.Preferences
        preferencesChild.component.onThemeStateChanged(ThemeState.DARK)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(testComponent.themeState.value).isEqualTo(ThemeState.DARK)
    }

    @Test
    fun `when onboarding flow completed then current child is home`() = runTest(testScheduler) {
        // given
        // when
        completeOnboardingFlow(component)
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when Preferences clicked then navigation stack updated`() = runTest(testScheduler) {
        // given
        completeOnboardingFlow(component)
        switchTab(HomeScreenTab.MAIN)
        val homeScreenChild = component.childStack.active.instance as RootComponent.Child.HomeScreen
        val currentTabChild = homeScreenChild.component.childStack.active.instance as HomeScreenComponent.Child.MainTab
        // when
        currentTabChild.component.onPreferencesClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.Preferences).isTrue()
        // when
        (component.childStack.active.instance as RootComponent.Child.Preferences).component.onBackClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when block A clicked then navigation stack updated`() = runTest(testScheduler) {
        // given
        completeOnboardingFlow(component)
        switchTab(HomeScreenTab.TRAINING)
        val homeScreenChild = component.childStack.active.instance as RootComponent.Child.HomeScreen
        val currentTabChild = homeScreenChild.component.childStack.active.instance as HomeScreenComponent.Child.TrainingsTab
        // when
        currentTabChild.component.onBlockAClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.BlockA).isTrue()
        // when
        (component.childStack.active.instance as RootComponent.Child.BlockA).component.onBackClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when block B clicked then navigation stack updated`() = runTest(testScheduler) {
        // given
        completeOnboardingFlow(component)
        switchTab(HomeScreenTab.TRAINING)
        val homeScreenChild = component.childStack.active.instance as RootComponent.Child.HomeScreen
        val currentTabChild = homeScreenChild.component.childStack.active.instance as HomeScreenComponent.Child.TrainingsTab
        // when
        currentTabChild.component.onBlockBClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.BlockB).isTrue()
        // when
        (component.childStack.active.instance as RootComponent.Child.BlockB).component.onBackClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when block C clicked then navigation stack updated`() = runTest(testScheduler) {
        // given
        completeOnboardingFlow(component)
        switchTab(HomeScreenTab.TRAINING)
        val homeScreenChild = component.childStack.active.instance as RootComponent.Child.HomeScreen
        val currentTabChild = homeScreenChild.component.childStack.active.instance as HomeScreenComponent.Child.TrainingsTab
        // when
        currentTabChild.component.onBlockCClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.BlockC).isTrue()
        // when
        (component.childStack.active.instance as RootComponent.Child.BlockC).component.onBackClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when Achievements clicked then navigation stack updated`() = runTest(testScheduler) {
        // given
        completeOnboardingFlow(component)
        switchTab(HomeScreenTab.PROGRESS)
        val homeScreenChild = component.childStack.active.instance as RootComponent.Child.HomeScreen
        val currentTabChild = homeScreenChild.component.childStack.active.instance as HomeScreenComponent.Child.ProgressTab
        // when
        currentTabChild.component.onAchievementsClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.Achievements).isTrue()
        // when
        (component.childStack.active.instance as RootComponent.Child.Achievements).component.onBackClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when Garden clicked then navigation stack updated`() = runTest(testScheduler) {
        // given
        completeOnboardingFlow(component)
        switchTab(HomeScreenTab.PROGRESS)
        val homeScreenChild = component.childStack.active.instance as RootComponent.Child.HomeScreen
        val currentTabChild = homeScreenChild.component.childStack.active.instance as HomeScreenComponent.Child.ProgressTab
        // when
        currentTabChild.component.onGardenClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.Garden).isTrue()
        // when
        (component.childStack.active.instance as RootComponent.Child.Garden).component.onBackClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when AddNewReminder clicked then navigation stack updated`() = runTest(testScheduler) {
        // given
        completeOnboardingFlow(component)
        switchTab(HomeScreenTab.REMINDERS)
        val homeScreenChild = component.childStack.active.instance as RootComponent.Child.HomeScreen
        val currentTabChild = homeScreenChild.component.childStack.active.instance as HomeScreenComponent.Child.RemindersTab
        // when
        currentTabChild.component.onAddNewClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.AddNewReminder).isTrue()
        // when
        (component.childStack.active.instance as RootComponent.Child.AddNewReminder).component.onBackClick()
        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when root back called from nested screen then navigation stack updated`() = runTest(testScheduler) {
        // given
        completeOnboardingFlow(component)
        switchTab(HomeScreenTab.PROGRESS)
        val homeScreenChild = component.childStack.active.instance as RootComponent.Child.HomeScreen
        val currentTabChild = homeScreenChild.component.childStack.active.instance as HomeScreenComponent.Child.ProgressTab
        currentTabChild.component.onGardenClick()
        assertThat(component.childStack.active.instance is RootComponent.Child.Garden).isTrue()

        // when
        component.onBack()

        // then
        assertThat(component.childStack.active.instance is RootComponent.Child.HomeScreen).isTrue()
    }

    @Test
    fun `when child emits error then root keeps current child`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("permission check failed")
        prepareStep5Dependencies()
        dev.mokkery.everySuspend { notifierMock.isNotificationPermissionGranted() } throws exception

        // when
        val before = component.childStack.active.instance::class
        navigateToStep5(component)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.childStack.active.instance::class).isEqualTo(before)
    }

    private fun completeOnboardingFlow(currentComponent: RootComponent) {
        val onboardingComponent = currentComponent.childStack.active.instance as? RootComponent.Child.Onboarding ?: return
        val step1 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step1
        step1.component.onNextClick()
        val step2 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step2
        step2.component.onNextClick()
        val step3 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step3
        step3.component.onNextClick()
        val step4 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step4
        step4.component.onNextClick()
        val step5 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step5
        step5.component.onNextClick()
    }

    private fun navigateToStep5(currentComponent: RootComponent) {
        val onboardingComponent = currentComponent.childStack.active.instance as? RootComponent.Child.Onboarding ?: return
        val step1 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step1
        step1.component.onNextClick()
        val step2 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step2
        step2.component.onNextClick()
        val step3 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step3
        step3.component.onNextClick()
        val step4 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step4
        step4.component.onNextClick()
    }

    private fun prepareStep5Dependencies() {
        dev.mokkery.every { notifierMock.permissionEvents() } returns emptyFlow()
        dev.mokkery.every { reminderManagerMock.createdReminders() } returns emptyFlow()
    }

    private fun switchTab(tab: HomeScreenTab) {
        val homeScreenChild = component.childStack.active.instance as? RootComponent.Child.HomeScreen ?: return
        homeScreenChild.component.onTabClick(tab)
    }

    override fun createComponent(): RootComponent =
        RootComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            alarmManager = alarmManagerMock,
            database = databaseMock,
            dispatchers = testDispatchers,
            notifier = notifierMock,
            settings = fakeSettings,
            timeUtils = timeUtilsMock,
            achievementsWatcher = achievementsWatcherMock,
            calendarWatcher = calendarWatcherMock,
            exerciseManager = exerciseManagerMock,
            highlightsProvider = highlightsProviderMock,
            reminderManager = reminderManagerMock,
            treeProgressWatcher = treeProgressWatcherMock,
        )

    private class FakeSettings : BlinklySettings {
        override var blinkBreakCount: Int = 60
        override var nearFarFocusCount: Int = 10
        override var nearFarFocusDuration: Float = 5f
        override var diagonalGazesCount: Int = 5
        override var diagonalGazesDuration: Float = 3f
        override var figureEightCount: Int = 10
        override var clockRollsEachSide: Int = 5
        override var palmingDuration: Int = 120
        override var themeState: ThemeState = ThemeState.SYSTEM
        override var lightThemeWorkoutIndex: Int = 0
        override var darkThemeWorkoutIndex: Int = 0
        override var lastTreeProgressCheckDate: LocalDate? = null
        override var displayedHighlights: List<Int> = emptyList()
        override var currentHighlightDate: LocalDate? = null
        override var onboardingDisplayed: Boolean = false
    }
}
