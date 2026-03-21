package com.sedsoftware.blinkly.component.home

import assertk.assertThat
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.active
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.home.integration.HomeScreenComponentDefault
import com.sedsoftware.blinkly.component.home.model.HomeScreenTab
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class HomeScreenComponentTest : ComponentTest<HomeScreenComponent>() {

    private val settingsMock: BlinklySettings = mock()
    private val fakeSettings = FakeSettings(settingsMock = settingsMock)

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
        component.onTabClick(HomeScreenTab.TRAININGS)
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

    override fun createComponent(): HomeScreenComponent =
        HomeScreenComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            dispatchers = testDispatchers,
            settings = fakeSettings,
            homeScreenOutput = { componentOutput.add(it) },
        )
}
