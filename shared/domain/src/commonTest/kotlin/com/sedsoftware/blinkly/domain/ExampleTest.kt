package com.sedsoftware.blinkly.domain

import kotlin.test.Test
import kotlin.test.assertTrue

@Suppress("MemberNameEqualsClassName", "FunctionOnlyReturningConstant")
class ExampleTest {
    private val example = Example()

    @Test
    fun exampleTest() {
        assertTrue {
            example.foo()
        }
    }
}
