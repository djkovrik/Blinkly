package com.sedsoftware.blinkly.domain

import kotlin.test.Test
import kotlin.test.assertTrue

class ExampleTest {
    private val example = Example()

    @Test
    fun exampleTest() {
        assertTrue {
            example.foo()
        }
    }
}
