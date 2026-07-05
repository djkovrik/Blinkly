package com.sedsoftware.blinkly.compose.ui.extension

import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.week_friday
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelsExtTest {

    @Test
    fun fridayUsesFridayLabelResource() {
        assertEquals(
            expected = Res.string.week_friday,
            actual = DayOfWeek.FRIDAY.asLabelResource(),
        )
    }
}
