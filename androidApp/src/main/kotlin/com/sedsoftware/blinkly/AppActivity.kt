package com.sedsoftware.blinkly

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.arkivanov.decompose.defaultComponentContext
import com.sedsoftware.blinkly.component.root.RootComponentFactory
import com.sedsoftware.blinkly.compose.ui.RootContent
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType
import dev.icerock.moko.permissions.PermissionsController

class AppActivity : ComponentActivity() {

    private val permissionsController: PermissionsController by lazy {
        PermissionsController(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val rootComponent = RootComponentFactory(
            componentContext = defaultComponentContext(),
            contentConfigurations = getNotificationConfigurations(),
            permissionsController = permissionsController,
            context = applicationContext,
        )

        setContent {
            RootContent(
                component = rootComponent,
                onThemeChanged = { ThemeChanged(it) },
            )
        }
    }

    private fun getNotificationConfigurations(): Map<ReminderType, ReminderConfig> {
        return mapOf(
            ReminderType.TWENTY_X3 to ReminderConfig(
                title = resources.getString(R.string.notification_title),
                description = resources.getString(R.string.notification_description),
            )
        )
    }
}

@Composable
private fun ThemeChanged(isDark: Boolean) {
    val view = LocalView.current
    LaunchedEffect(isDark) {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = isDark
            isAppearanceLightNavigationBars = isDark
        }
    }
}
