@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.component.achievements

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.sedsoftware.blinkly.component.ComponentTest
import com.sedsoftware.blinkly.component.achievements.integration.AchievementsComponentDefault
import com.sedsoftware.blinkly.domain.BlinklyAchievementsWatcher
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ComponentOutput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.time.Instant

class AchievementsComponentTest : ComponentTest<AchievementsComponent>() {

    private val achievementsFlow: MutableStateFlow<List<Achievement>> = MutableStateFlow(emptyList())

    @Test
    fun `when achievements emitted then model contains grouped achievements in level order`() = runTest(testScheduler) {
        // given
        achievementsFlow.value = listOf(
            achievement(AchievementType.EARLY_BIRD, AchievementLevel.HIDDEN),
            achievement(AchievementType.DIAMOND_EYES, AchievementLevel.INTERMEDIATE, unlockedAt = UNLOCKED_AT),
            achievement(AchievementType.FIRST_SPARK, AchievementLevel.BEGINNER, unlockedAt = UNLOCKED_AT),
            achievement(AchievementType.IRON_GAZE, AchievementLevel.PRO),
            achievement(AchievementType.FALCON_EYE, AchievementLevel.EXPERT),
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        val model = component.model.value
        assertThat(model.sections.map { it.level }).isEqualTo(
            listOf(
                AchievementLevel.BEGINNER,
                AchievementLevel.INTERMEDIATE,
                AchievementLevel.PRO,
                AchievementLevel.EXPERT,
                AchievementLevel.HIDDEN,
            )
        )
        assertThat(model.sections.flatMap { it.achievements }.map { it.type }).isEqualTo(
            listOf(
                AchievementType.FIRST_SPARK,
                AchievementType.DIAMOND_EYES,
                AchievementType.IRON_GAZE,
                AchievementType.FALCON_EYE,
                AchievementType.EARLY_BIRD,
            )
        )
    }

    @Test
    fun `when achievements emitted then locked and hidden display flags are mapped`() = runTest(testScheduler) {
        // given
        achievementsFlow.value = listOf(
            achievement(AchievementType.FIRST_SPARK, AchievementLevel.BEGINNER, unlockedAt = UNLOCKED_AT),
            achievement(AchievementType.BLINK_STARTER, AchievementLevel.BEGINNER),
            achievement(AchievementType.EARLY_BIRD, AchievementLevel.HIDDEN),
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        val items = component.model.value.sections.flatMap { it.achievements }.associateBy { it.type }
        assertThat(items.getValue(AchievementType.FIRST_SPARK).isUnlocked).isEqualTo(true)
        assertThat(items.getValue(AchievementType.FIRST_SPARK).isDetailsAvailable).isEqualTo(true)
        assertThat(items.getValue(AchievementType.FIRST_SPARK).isDescriptionHidden).isEqualTo(false)
        assertThat(items.getValue(AchievementType.BLINK_STARTER).isUnlocked).isEqualTo(false)
        assertThat(items.getValue(AchievementType.BLINK_STARTER).isDetailsAvailable).isEqualTo(false)
        assertThat(items.getValue(AchievementType.BLINK_STARTER).isDescriptionHidden).isEqualTo(false)
        assertThat(items.getValue(AchievementType.EARLY_BIRD).isUnlocked).isEqualTo(false)
        assertThat(items.getValue(AchievementType.EARLY_BIRD).isDetailsAvailable).isEqualTo(false)
        assertThat(items.getValue(AchievementType.EARLY_BIRD).isDescriptionHidden).isEqualTo(true)
    }

    @Test
    fun `when unlocked achievement clicked then details are shown and can be dismissed`() = runTest(testScheduler) {
        // given
        achievementsFlow.value = listOf(
            achievement(AchievementType.FIRST_SPARK, AchievementLevel.BEGINNER, unlockedAt = UNLOCKED_AT),
        )
        testScheduler.advanceUntilIdle()

        // when
        component.onAchievementClick(AchievementType.FIRST_SPARK)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.selectedAchievement).isNotNull()
        assertThat(component.model.value.selectedAchievement?.type).isEqualTo(AchievementType.FIRST_SPARK)

        // when
        component.onDetailsDismiss()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.selectedAchievement).isNull()
    }

    @Test
    fun `when locked achievements clicked then details are not shown`() = runTest(testScheduler) {
        // given
        achievementsFlow.value = listOf(
            achievement(AchievementType.BLINK_STARTER, AchievementLevel.BEGINNER),
            achievement(AchievementType.EARLY_BIRD, AchievementLevel.HIDDEN),
        )
        testScheduler.advanceUntilIdle()

        // when
        component.onAchievementClick(AchievementType.BLINK_STARTER)
        component.onAchievementClick(AchievementType.EARLY_BIRD)
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.selectedAchievement).isNull()
    }

    @Test
    fun `when selected achievement becomes locked then details are hidden`() = runTest(testScheduler) {
        // given
        achievementsFlow.value = listOf(
            achievement(AchievementType.FIRST_SPARK, AchievementLevel.BEGINNER, unlockedAt = UNLOCKED_AT),
        )
        testScheduler.advanceUntilIdle()
        component.onAchievementClick(AchievementType.FIRST_SPARK)
        testScheduler.advanceUntilIdle()

        // when
        achievementsFlow.value = listOf(
            achievement(AchievementType.FIRST_SPARK, AchievementLevel.BEGINNER),
        )
        testScheduler.advanceUntilIdle()

        // then
        assertThat(component.model.value.selectedAchievement).isNull()
    }

    @Test
    fun `when back clicked then component emits back output`() = runTest(testScheduler) {
        // when
        component.onBackClick()

        // then
        assertThat(componentOutput).contains(ComponentOutput.Common.BackPressed)
    }

    @Test
    fun `when achievements watcher fails then component publishes error output`() = runTest(testScheduler) {
        // given
        val exception = IllegalStateException("achievements failed")
        val testComponent = createComponent(
            watcher = object : BlinklyAchievementsWatcher {
                override val achievements: Flow<List<Achievement>> = flow { throw exception }
            }
        )

        // when
        testScheduler.advanceUntilIdle()

        // then
        assertThat(testComponent).isInstanceOf(AchievementsComponent::class)
        assertThat(
            componentOutput.filterIsInstance<ComponentOutput.Common.ErrorCaught>()
                .any { it.throwable.message == exception.message }
        ).isTrue()
    }

    override fun createComponent(): AchievementsComponent =
        createComponent(
            watcher = object : BlinklyAchievementsWatcher {
                override val achievements: Flow<List<Achievement>> = achievementsFlow
            }
        )

    private fun createComponent(
        watcher: BlinklyAchievementsWatcher,
    ): AchievementsComponent =
        AchievementsComponentDefault(
            componentContext = DefaultComponentContext(lifecycle),
            storeFactory = DefaultStoreFactory(),
            dispatchers = testDispatchers,
            achievementsWatcher = watcher,
            achievementsOutput = { componentOutput.add(it) },
        )

    private fun achievement(
        type: AchievementType,
        level: AchievementLevel,
        unlockedAt: Instant? = null,
    ): Achievement =
        Achievement(
            type = type,
            level = level,
            unlockedAt = unlockedAt,
        )

    private companion object {
        val UNLOCKED_AT: Instant = LocalDateTime(2026, 3, 15, 12, 30).toInstant(TimeZone.UTC)
    }
}
