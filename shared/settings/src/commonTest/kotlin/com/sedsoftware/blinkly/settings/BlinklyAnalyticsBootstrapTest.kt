package com.sedsoftware.blinkly.settings

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.russhwolf.settings.MapSettings
import kotlin.test.Test

class BlinklyAnalyticsBootstrapTest {

    @Test
    fun `fresh installation defaults analytics to enabled`() {
        val state = readBlinklyAnalyticsBootstrapState(MapSettings())

        assertThat(state.analyticsEnabled).isTrue()
        assertThat(state.existingInstallation).isFalse()
    }

    @Test
    fun `persisted opt out is restored`() {
        val settings = MapSettings().apply { putBoolean("ae", false) }

        val state = readBlinklyAnalyticsBootstrapState(settings)

        assertThat(state.analyticsEnabled).isFalse()
    }

    @Test
    fun `database or existing preference marks an update installation`() {
        val settings = MapSettings().apply { putBoolean("od", true) }

        assertThat(readBlinklyAnalyticsBootstrapState(settings).existingInstallation).isTrue()
        assertThat(
            readBlinklyAnalyticsBootstrapState(MapSettings(), databaseExists = true).existingInstallation
        ).isTrue()
    }
}
