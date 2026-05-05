package com.sedsoftware.blinkly.component.step4.store

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.sedsoftware.blinkly.component.step4.store.DisclaimerStore.Intent
import com.sedsoftware.blinkly.component.step4.store.DisclaimerStore.State
import com.sedsoftware.blinkly.utils.StoreProvider
import kotlin.coroutines.CoroutineContext

internal class DisclaimerStoreProvider(
    private val storeFactory: StoreFactory,
    private val mainContext: CoroutineContext,
) {

    @StoreProvider
    fun create(autoInit: Boolean = true): DisclaimerStore =
        object : DisclaimerStore, Store<Intent, State, Nothing> by storeFactory.create<Intent, Nothing, Msg, State, Nothing>(
            name = "DisclaimerStore",
            initialState = State(),
            autoInit = autoInit,
            executorFactory = coroutineExecutorFactory(mainContext) {
                onIntent<Intent.OnCheckboxChecked> {
                    dispatch(Msg.CheckboxValueChanged(it.checked))
                }
            },
            reducer = { msg ->
                when (msg) {
                    is Msg.CheckboxValueChanged -> copy(
                        checkboxSelected = msg.checked,
                    )
                }
            }
        ) {}

    private sealed interface Msg {
        data class CheckboxValueChanged(val checked: Boolean) : Msg
    }
}
