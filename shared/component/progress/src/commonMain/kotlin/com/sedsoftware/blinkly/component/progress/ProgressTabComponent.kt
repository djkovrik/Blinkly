package com.sedsoftware.blinkly.component.progress

import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Tree
import kotlinx.datetime.LocalDate

interface ProgressTabComponent {

    val model: Value<Model>

    fun onAchievementsClick()
    fun onGardenClick()

    data class Model(
        val calendarWeeks: List<List<CalendarDay?>>,
        val tree: Tree?,
        val recentAchievements: List<Achievement?>,
    )

    data class CalendarDay(
        val date: LocalDate,
        val state: CalendarDayState,
    )

    enum class CalendarDayState {
        EMPTY,
        WORKOUT,
        PERFECT,
    }
}
