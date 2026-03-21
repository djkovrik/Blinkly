package com.sedsoftware.blinkly.component.root

import assertk.assertThat
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.home.HomeScreenComponent
import com.sedsoftware.blinkly.component.home.model.HomeScreenTab
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent
import com.sedsoftware.blinkly.component.root.integration.RootComponentDefault
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class RootComponentTest : ComponentTest<RootComponent>() {

    private val alarmManagerMock: BlinklyAlarmManager = mock()
    private val databaseMock: BlinklyDatabase = mock()
    private val notifierMock: BlinklyNotifier = mock()
    private val settingsMock: BlinklySettings = mock()
    private val fakeSettings = FakeSettings(settingsMock = settingsMock)
    private val timeUtilsMock: BlinklyTimeUtils = mock()

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
        switchTab(HomeScreenTab.TRAININGS)
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
        switchTab(HomeScreenTab.TRAININGS)
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
        switchTab(HomeScreenTab.TRAININGS)
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

    private fun completeOnboardingFlow(currentComponent: RootComponent) {
        val onboardingComponent = currentComponent.childStack.active.instance as? RootComponent.Child.Onboarding ?: return
        val step1 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step1
        step1.component.nextStep()
        val step2 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step2
        step2.component.nextStep()
        val step3 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step3
        step3.component.nextStep()
        val step4 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step4
        step4.component.nextStep()
        val step5 = onboardingComponent.component.childStack.active.instance as OnboardingComponent.Child.Step5
        step5.component.nextStep()
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
        )
}
