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
import com.sedsoftware.blinkly.domain.model.ThemeState
import com.sedsoftware.blinkly.utils.StoreProvider
import com.sedsoftware.blinkly.utils.unwrap
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
                onAction<Action.LoadPreferences> {
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.load() },
                            onSuccess = { data -> dispatch(Msg.PreferencesChanged(data)) },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }

                onIntent<Intent.BlinkBreakCountChanged> {
                    val value = it.value.atLeastOne()
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.saveBlinkBreakCount(value) },
                            onSuccess = { dispatch(Msg.BlinkBreakCountChanged(value)) },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }

                onIntent<Intent.NearFarFocusCountChanged> {
                    val value = it.value.atLeastOne()
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.saveNearFarFocusCount(value) },
                            onSuccess = { dispatch(Msg.NearFarFocusCountChanged(value)) },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }

                onIntent<Intent.NearFarFocusDurationChanged> {
                    val value = it.value.atLeastHalf()
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.saveNearFarFocusDuration(value) },
                            onSuccess = { dispatch(Msg.NearFarFocusDurationChanged(value)) },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }

                onIntent<Intent.DiagonalGazesCountChanged> {
                    val value = it.value.atLeastOne()
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.saveDiagonalGazesCount(value) },
                            onSuccess = { dispatch(Msg.DiagonalGazesCountChanged(value)) },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }

                onIntent<Intent.DiagonalGazesDurationChanged> {
                    val value = it.value.atLeastHalf()
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.saveDiagonalGazesDuration(value) },
                            onSuccess = { dispatch(Msg.DiagonalGazesDurationChanged(value)) },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }

                onIntent<Intent.FigureEightCountChanged> {
                    val value = it.value.atLeastOne()
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.saveFigureEightCount(value) },
                            onSuccess = { dispatch(Msg.FigureEightCountChanged(value)) },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }

                onIntent<Intent.ClockRollsEachSideChanged> {
                    val value = it.value.atLeastOne()
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.saveClockRollsEachSide(value) },
                            onSuccess = { dispatch(Msg.ClockRollsEachSideChanged(value)) },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }

                onIntent<Intent.PalmingDurationChanged> {
                    val value = it.value.atLeastOne()
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.savePalmingDuration(value) },
                            onSuccess = { dispatch(Msg.PalmingDurationChanged(value)) },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }

                onIntent<Intent.ThemeStateChanged> {
                    val value = it.value
                    launch {
                        unwrap(
                            result = withContext(ioContext) { manager.saveThemeState(value) },
                            onSuccess = {
                                dispatch(Msg.ThemeStateChanged(value))
                                publish(Label.ThemeStateChanged(value))
                            },
                            onError = { throwable -> publish(Label.ErrorCaught(throwable)) },
                        )
                    }
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.PreferencesChanged -> copy(
                        blinkBreakCount = msg.data.blinkBreakCount,
                        nearFarFocusCount = msg.data.nearFarFocusCount,
                        nearFarFocusDuration = msg.data.nearFarFocusDuration,
                        diagonalGazesCount = msg.data.diagonalGazesCount,
                        diagonalGazesDuration = msg.data.diagonalGazesDuration,
                        figureEightCount = msg.data.figureEightCount,
                        clockRollsEachSide = msg.data.clockRollsEachSide,
                        palmingDuration = msg.data.palmingDuration,
                        themeState = msg.data.themeState,
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

    sealed interface Msg {
        data class PreferencesChanged(val data: PreferencesData) : Msg
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
