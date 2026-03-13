package com.sedsoftware.blinkly.domain.model

enum class ExerciseBlock(val index: Int) {
    A(1),
    B(2),
    C(3);

    companion object {
        fun fromIndex(index: Int): ExerciseBlock =
            entries.firstOrNull { it.index == index } ?: A
    }
}
