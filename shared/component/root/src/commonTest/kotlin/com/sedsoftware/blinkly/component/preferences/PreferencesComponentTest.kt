package com.sedsoftware.blinkly.component.preferences

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.preferences.integration.PreferencesComponentDefault
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class PreferencesComponentTest : ComponentTest<PreferencesComponent>() {

    private val settings: FakeSettings = FakeSettings()

    @Test
    fun `when component created then model contains settings values`() = runTest(testScheduler) {
        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value).isEqualTo(
            PreferencesComponent.Model(
                blinkBreakCount = 60,
                nearFarFocusCount = 10,
                nearFarFocusDuration = 5f,
                diagonalGazesCount = 5,
                diagonalGazesDuration = 3f,
                figureEightCount = 10,
                clockRollsEachSide = 5,
                palmingDuration = 120,
                themeState = ThemeState.SYSTEM,
            )
        )
    }

    @Test
    fun `when numeric preferences changed then settings and model are updated`() = runTest(testScheduler) {
        // given
        testScheduler.advanceUntilIdle()

        // when
        component.onBlinkBreakCountChanged(80)
        component.onNearFarFocusDurationChanged(6.5f)
        component.onPalmingDurationChanged(180)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(settings.blinkBreakCount).isEqualTo(80)
        assertThat(settings.nearFarFocusDuration).isEqualTo(6.5f)
        assertThat(settings.palmingDuration).isEqualTo(180)
        assertThat(component.model.value.blinkBreakCount).isEqualTo(80)
        assertThat(component.model.value.nearFarFocusDuration).isEqualTo(6.5f)
        assertThat(component.model.value.palmingDuration).isEqualTo(180)
    }

    @Test
    fun `when theme changed then settings and model are updated`() = runTest(testScheduler) {
        // given
        testScheduler.advanceUntilIdle()

        // when
        component.onThemeStateChanged(ThemeState.LIGHT)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(settings.themeState).isEqualTo(ThemeState.LIGHT)
        assertThat(component.model.value.themeState).isEqualTo(ThemeState.LIGHT)
        assertThat(componentOutput).contains(ComponentOutput.Preferences.ThemeStateChanged(ThemeState.LIGHT))
    }

    @Test
    fun `when invalid low value selected then value is clamped`() = runTest(testScheduler) {
        // given
        testScheduler.advanceUntilIdle()

        // when
        component.onDiagonalGazesDurationChanged(0f)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(settings.diagonalGazesDuration).isEqualTo(0.5f)
        assertThat(component.model.value.diagonalGazesDuration).isEqualTo(0.5f)
    }

    @Test
    fun `when back clicked then component emits back output`() = runTest(testScheduler) {
        // when
        component.onBackClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Common.BackPressed)
    }

    override fun createComponent(): PreferencesComponent =
        PreferencesComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            settings = settings,
            preferencesOutput = { componentOutput.add(it) },
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
