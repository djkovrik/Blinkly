package com.sedsoftware.blinkly.component.preferences.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.preferences.domain.PreferencesManager
import com.sedsoftware.blinkly.component.preferences.domain.model.PreferencesData
import com.sedsoftware.blinkly.component.preferences.store.PreferencesStore.Intent
import com.sedsoftware.blinkly.component.preferences.store.PreferencesStore.Label
import com.sedsoftware.blinkly.component.preferences.store.PreferencesStore.State
import com.sedsoftware.blinkly.domain.model.BlinklyError
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.utils.StoreProvider
import com.sedsoftware.blinkly.utils.unwrap
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class PreferencesStoreProvider(
    private val storeFactory: StoreFactory,
    private val manager: PreferencesManager,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
) {
    @StoreProvider
    fun create(autoInit: Boolean = true): PreferencesStore =
        object : PreferencesStore, Store<Intent, State, Label> by storeFactory.create<Intent, Action, Msg, State, Label>(
            name = "PreferencesStore",
            initialState = State(),
            autoInit = autoInit,
            bootstrapper = coroutineBootstrapper(mainContext) {
                dispatch(Action.LoadPreferences)
            },
            executorFactory = coroutineExecutorFactory(mainContext) {
                var dirtyFlags = DirtyFlags()
                var saveJob: Job? = null

                onAction<Action.LoadPreferences> {
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.load() },
                            onSuccess = { data -> dispatch(Msg.PreferencesChanged(data, dirtyFlags)) },
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesLoading(throwable))) },
                        )
                    }
                }

                onIntent<Intent.BlinkBreakCountChanged> {
                    val value = it.value.atLeastOne()
                    dirtyFlags = dirtyFlags.copy(blinkBreakCount = true)
                    dispatch(Msg.BlinkBreakCountChanged(value))
                    val previousJob = saveJob
                    saveJob = launch {
                        previousJob?.join()
                        unwrap(
                            result = withContext(ioContext) { manager.saveBlinkBreakCount(value) },
                            onSuccess = {},
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesSaving(throwable))) },
                        )
                    }
                }

                onIntent<Intent.NearFarFocusCountChanged> {
                    val value = it.value.atLeastOne()
                    dirtyFlags = dirtyFlags.copy(nearFarFocusCount = true)
                    dispatch(Msg.NearFarFocusCountChanged(value))
                    val previousJob = saveJob
                    saveJob = launch {
                        previousJob?.join()
                        unwrap(
                            result = withContext(ioContext) { manager.saveNearFarFocusCount(value) },
                            onSuccess = {},
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesSaving(throwable))) },
                        )
                    }
                }

                onIntent<Intent.NearFarFocusDurationChanged> {
                    val value = it.value.atLeastHalf()
                    dirtyFlags = dirtyFlags.copy(nearFarFocusDuration = true)
                    dispatch(Msg.NearFarFocusDurationChanged(value))
                    val previousJob = saveJob
                    saveJob = launch {
                        previousJob?.join()
                        unwrap(
                            result = withContext(ioContext) { manager.saveNearFarFocusDuration(value) },
                            onSuccess = {},
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesSaving(throwable))) },
                        )
                    }
                }

                onIntent<Intent.DiagonalGazesCountChanged> {
                    val value = it.value.atLeastOne()
                    dirtyFlags = dirtyFlags.copy(diagonalGazesCount = true)
                    dispatch(Msg.DiagonalGazesCountChanged(value))
                    val previousJob = saveJob
                    saveJob = launch {
                        previousJob?.join()
                        unwrap(
                            result = withContext(ioContext) { manager.saveDiagonalGazesCount(value) },
                            onSuccess = {},
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesSaving(throwable))) },
                        )
                    }
                }

                onIntent<Intent.DiagonalGazesDurationChanged> {
                    val value = it.value.atLeastHalf()
                    dirtyFlags = dirtyFlags.copy(diagonalGazesDuration = true)
                    dispatch(Msg.DiagonalGazesDurationChanged(value))
                    val previousJob = saveJob
                    saveJob = launch {
                        previousJob?.join()
                        unwrap(
                            result = withContext(ioContext) { manager.saveDiagonalGazesDuration(value) },
                            onSuccess = {},
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesSaving(throwable))) },
                        )
                    }
                }

                onIntent<Intent.FigureEightCountChanged> {
                    val value = it.value.atLeastOne()
                    dirtyFlags = dirtyFlags.copy(figureEightCount = true)
                    dispatch(Msg.FigureEightCountChanged(value))
                    val previousJob = saveJob
                    saveJob = launch {
                        previousJob?.join()
                        unwrap(
                            result = withContext(ioContext) { manager.saveFigureEightCount(value) },
                            onSuccess = {},
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesSaving(throwable))) },
                        )
                    }
                }

                onIntent<Intent.ClockRollsEachSideChanged> {
                    val value = it.value.atLeastOne()
                    dirtyFlags = dirtyFlags.copy(clockRollsEachSide = true)
                    dispatch(Msg.ClockRollsEachSideChanged(value))
                    val previousJob = saveJob
                    saveJob = launch {
                        previousJob?.join()
                        unwrap(
                            result = withContext(ioContext) { manager.saveClockRollsEachSide(value) },
                            onSuccess = {},
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesSaving(throwable))) },
                        )
                    }
                }

                onIntent<Intent.PalmingDurationChanged> {
                    val value = it.value.atLeastOne()
                    dirtyFlags = dirtyFlags.copy(palmingDuration = true)
                    dispatch(Msg.PalmingDurationChanged(value))
                    val previousJob = saveJob
                    saveJob = launch {
                        previousJob?.join()
                        unwrap(
                            result = withContext(ioContext) { manager.savePalmingDuration(value) },
                            onSuccess = {},
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesSaving(throwable))) },
                        )
                    }
                }

                onIntent<Intent.ThemeStateChanged> {
                    val value = it.value
                    dirtyFlags = dirtyFlags.copy(themeState = true)
                    dispatch(Msg.ThemeStateChanged(value))
                    val previousJob = saveJob
                    saveJob = launch {
                        previousJob?.join()
                        unwrap(
                            result = withContext(ioContext) { manager.saveThemeState(value) },
                            onSuccess = {
                                if (state().themeState == value) {
                                    publish(Label.ThemeStateChanged(value))
                                }
                            },
                            onError = { throwable -> publish(Label.ErrorCaught(BlinklyError.PreferencesSaving(throwable))) },
                        )
                    }
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.PreferencesChanged -> copy(
                        blinkBreakCount = if (msg.dirtyFlags.blinkBreakCount) blinkBreakCount else msg.data.blinkBreakCount,
                        nearFarFocusCount = if (msg.dirtyFlags.nearFarFocusCount) nearFarFocusCount else msg.data.nearFarFocusCount,
                        nearFarFocusDuration = if (msg.dirtyFlags.nearFarFocusDuration) {
                            nearFarFocusDuration
                        } else {
                            msg.data.nearFarFocusDuration
                        },
                        diagonalGazesCount = if (msg.dirtyFlags.diagonalGazesCount) {
                            diagonalGazesCount
                        } else {
                            msg.data.diagonalGazesCount
                        },
                        diagonalGazesDuration = if (msg.dirtyFlags.diagonalGazesDuration) {
                            diagonalGazesDuration
                        } else {
                            msg.data.diagonalGazesDuration
                        },
                        figureEightCount = if (msg.dirtyFlags.figureEightCount) {
                            figureEightCount
                        } else {
                            msg.data.figureEightCount
                        },
                        clockRollsEachSide = if (msg.dirtyFlags.clockRollsEachSide) {
                            clockRollsEachSide
                        } else {
                            msg.data.clockRollsEachSide
                        },
                        palmingDuration = if (msg.dirtyFlags.palmingDuration) palmingDuration else msg.data.palmingDuration,
                        themeState = if (msg.dirtyFlags.themeState) themeState else msg.data.themeState,
                    )

                    is Msg.BlinkBreakCountChanged -> copy(blinkBreakCount = msg.value)
                    is Msg.NearFarFocusCountChanged -> copy(nearFarFocusCount = msg.value)
                    is Msg.NearFarFocusDurationChanged -> copy(nearFarFocusDuration = msg.value)
                    is Msg.DiagonalGazesCountChanged -> copy(diagonalGazesCount = msg.value)
                    is Msg.DiagonalGazesDurationChanged -> copy(diagonalGazesDuration = msg.value)
                    is Msg.FigureEightCountChanged -> copy(figureEightCount = msg.value)
                    is Msg.ClockRollsEachSideChanged -> copy(clockRollsEachSide = msg.value)
                    is Msg.PalmingDurationChanged -> copy(palmingDuration = msg.value)
                    is Msg.ThemeStateChanged -> copy(themeState = msg.value)
                }
            }
        ) {}

    sealed interface Action {
        data object LoadPreferences : Action
    }

    data class DirtyFlags(
        val blinkBreakCount: Boolean = false,
        val nearFarFocusCount: Boolean = false,
        val nearFarFocusDuration: Boolean = false,
        val diagonalGazesCount: Boolean = false,
        val diagonalGazesDuration: Boolean = false,
        val figureEightCount: Boolean = false,
        val clockRollsEachSide: Boolean = false,
        val palmingDuration: Boolean = false,
        val themeState: Boolean = false,
    )

    sealed interface Msg {
        data class PreferencesChanged(val data: PreferencesData, val dirtyFlags: DirtyFlags) : Msg
        data class BlinkBreakCountChanged(val value: Int) : Msg
        data class NearFarFocusCountChanged(val value: Int) : Msg
        data class NearFarFocusDurationChanged(val value: Float) : Msg
        data class DiagonalGazesCountChanged(val value: Int) : Msg
        data class DiagonalGazesDurationChanged(val value: Float) : Msg
        data class FigureEightCountChanged(val value: Int) : Msg
        data class ClockRollsEachSideChanged(val value: Int) : Msg
        data class PalmingDurationChanged(val value: Int) : Msg
        data class ThemeStateChanged(val value: ThemeState) : Msg
    }
}

private fun Int.atLeastOne(): Int = coerceAtLeast(1)

@Suppress("MagicNumber")
private fun Float.atLeastHalf(): Float = coerceAtLeast(0.5f)
