package com.sedsoftware.blinkly.domain.exercise

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import com.sedsoftware.blinkly.domain.exercise.dsl.Blink
import com.sedsoftware.blinkly.domain.exercise.dsl.exercise
import com.sedsoftware.blinkly.domain.exercise.engine.ProgressCalculator
import com.sedsoftware.blinkly.domain.extension.ms
import com.sedsoftware.blinkly.domain.model.ExerciseType
import kotlin.test.Test

class ProgressCalculatorTest {

    @Test
    fun `when steps executed then percent increases`() {
        // given
        val script = exercise(ExerciseType.BLINK_BREAK) {
            Blink(1) every 100.ms
            Blink(2) every 100.ms
        }

        val calc = ProgressCalculator(script)

        // when
        val p1 = calc.step(100)
        val p2 = calc.step(100)

        // then
        assertThat(p1.percent).isLessThan(p2.percent)
        assertThat(p2.percent).isEqualTo(100)
    }
}
