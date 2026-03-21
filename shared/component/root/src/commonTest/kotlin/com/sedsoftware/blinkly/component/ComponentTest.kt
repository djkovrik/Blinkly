package com.sedsoftware.blinkly.component

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class ComponentTest<Component : Any> {

    protected val testScheduler: TestCoroutineScheduler = TestCoroutineScheduler()
    protected val lifecycle: LifecycleRegistry = LifecycleRegistry()
    protected val componentOutput: MutableList<ComponentOutput> = mutableListOf()

    protected val testDispatchers: BlinklyDispatchers =
        object : BlinklyDispatchers {
            override val main: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
            override val io: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
        }

    protected val component: Component
        get() = _component!!

    private var _component: Component? = null

    class FakeSettings(
        override var onboardingDisplayed: Boolean = false,
        val settingsMock: BlinklySettings,
    ) : BlinklySettings by settingsMock

    @BeforeTest
    fun setUp() {
        _component = createComponent()
        isAssertOnMainThreadEnabled = false
        lifecycle.create()
        lifecycle.resume()
    }

    @AfterTest
    fun tearDown() {
        _component = null
        isAssertOnMainThreadEnabled = true
        componentOutput.clear()
        lifecycle.pause()
        lifecycle.destroy()
    }

    abstract fun createComponent(): Component
}
