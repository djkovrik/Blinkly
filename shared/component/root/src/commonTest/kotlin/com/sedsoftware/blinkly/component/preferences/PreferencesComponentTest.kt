package com.sedsoftware.blinkly.component.preferences

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.preferences.integration.PreferencesComponentDefault
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklySyncManager
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.BlinklySyncState
import com.sedsoftware.blinkly.domain.model.BlinklyUser
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.time.Instant

class PreferencesComponentTest : ComponentTest<PreferencesComponent>() {

    private val settings: FakeSettings = FakeSettings()
    private val syncManager: FakeBlinklySyncManager = FakeBlinklySyncManager()

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
        component.onNearFarFocusCountChanged(12)
        component.onNearFarFocusDurationChanged(6.5f)
        component.onDiagonalGazesCountChanged(7)
        component.onDiagonalGazesDurationChanged(4f)
        component.onFigureEightCountChanged(11)
        component.onClockRollsEachSideChanged(6)
        component.onPalmingDurationChanged(180)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(settings.blinkBreakCount).isEqualTo(80)
        assertThat(settings.nearFarFocusCount).isEqualTo(12)
        assertThat(settings.nearFarFocusDuration).isEqualTo(6.5f)
        assertThat(settings.diagonalGazesCount).isEqualTo(7)
        assertThat(settings.diagonalGazesDuration).isEqualTo(4f)
        assertThat(settings.figureEightCount).isEqualTo(11)
        assertThat(settings.clockRollsEachSide).isEqualTo(6)
        assertThat(settings.palmingDuration).isEqualTo(180)
        assertThat(component.model.value.blinkBreakCount).isEqualTo(80)
        assertThat(component.model.value.nearFarFocusCount).isEqualTo(12)
        assertThat(component.model.value.nearFarFocusDuration).isEqualTo(6.5f)
        assertThat(component.model.value.diagonalGazesCount).isEqualTo(7)
        assertThat(component.model.value.diagonalGazesDuration).isEqualTo(4f)
        assertThat(component.model.value.figureEightCount).isEqualTo(11)
        assertThat(component.model.value.clockRollsEachSide).isEqualTo(6)
        assertThat(component.model.value.palmingDuration).isEqualTo(180)
    }

    @Test
    fun `when preferences changed during loading then loaded values do not overwrite edits`() = runTest(testScheduler) {
        // when
        component.onBlinkBreakCountChanged(80)
        component.onNearFarFocusCountChanged(12)
        component.onNearFarFocusDurationChanged(6.5f)
        component.onDiagonalGazesCountChanged(7)
        component.onDiagonalGazesDurationChanged(4f)
        component.onFigureEightCountChanged(11)
        component.onClockRollsEachSideChanged(6)
        component.onPalmingDurationChanged(180)
        component.onThemeStateChanged(ThemeState.DARK)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value).isEqualTo(
            PreferencesComponent.Model(
                blinkBreakCount = 80,
                nearFarFocusCount = 12,
                nearFarFocusDuration = 6.5f,
                diagonalGazesCount = 7,
                diagonalGazesDuration = 4f,
                figureEightCount = 11,
                clockRollsEachSide = 6,
                palmingDuration = 180,
                themeState = ThemeState.DARK,
            )
        )
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
        component.onDiagonalGazesCountChanged(0)
        component.onDiagonalGazesDurationChanged(0f)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(settings.diagonalGazesCount).isEqualTo(1)
        assertThat(settings.diagonalGazesDuration).isEqualTo(0.5f)
        assertThat(component.model.value.diagonalGazesCount).isEqualTo(1)
        assertThat(component.model.value.diagonalGazesDuration).isEqualTo(0.5f)
    }

    @Test
    fun `when loading preferences fails then component emits loading error`() = runTest(testScheduler) {
        // given
        val failure = IllegalStateException("load failed")
        val throwingSettings = object : BlinklySettings by settings {
            override var blinkBreakCount: Int
                get() = throw failure
                set(@Suppress("UNUSED_PARAMETER") value) = Unit
        }
        PreferencesComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            settings = throwingSettings,
            syncManager = syncManager,
            preferencesOutput = { componentOutput.add(it) },
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(componentOutputContainsErrorCausedBy<BlinklyError.PreferencesLoading>(failure)).isTrue()
    }

    @Test
    fun `when saving preference fails then component emits saving error`() = runTest(testScheduler) {
        // given
        testScheduler.advanceUntilIdle()
        val failure = IllegalStateException("save failed")
        settings.blinkBreakCountSaveFailure = failure

        // when
        component.onBlinkBreakCountChanged(80)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(componentOutputContainsErrorCausedBy<BlinklyError.PreferencesSaving>(failure)).isTrue()
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
            syncManager = syncManager,
            preferencesOutput = { componentOutput.add(it) },
        )

    private class FakeSettings : BlinklySettings {
        var blinkBreakCountSaveFailure: Throwable? = null

        override var blinkBreakCount: Int = 60
            set(value) {
                blinkBreakCountSaveFailure?.let { throw it }
                field = value
            }
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
        override var lastLocalDatabaseChangeAt: Instant? = null
        override var lastLocalSettingsChangeAt: Instant? = null
        override var lastSyncedAt: Instant? = null
        override var lastRemoteUpdatedAt: Instant? = null
    }

    private class FakeBlinklySyncManager : BlinklySyncManager {
        override val state: StateFlow<BlinklySyncState> = MutableStateFlow(
            BlinklySyncState(
                isAuthorized = false,
                isSyncing = false,
                lastSyncedAt = null,
                error = null,
            )
        )

        override suspend fun completeGoogleSignIn(user: BlinklyUser) = Unit
        override suspend fun syncNow() = Unit
    }
}
