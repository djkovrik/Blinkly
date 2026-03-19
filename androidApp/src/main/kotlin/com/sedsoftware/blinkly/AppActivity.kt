package com.sedsoftware.blinkly

import android.app.Activity
import android.app.NotificationManager
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.arkivanov.decompose.defaultComponentContext
import com.sedsoftware.blinkly.component.root.RootComponentFactory
import com.sedsoftware.blinkly.compose.ui.RootContent
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.tweener.alarmee.channel.AlarmeeNotificationChannel
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
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
            platformConfiguration = getPlatformConfiguration(),
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

    private fun getPlatformConfiguration(): AlarmeePlatformConfiguration {
        return AlarmeeAndroidPlatformConfiguration(
            notificationIconResId = R.drawable.ic_reminder,
            notificationIconColor = Color.White,
            useExactScheduling = false,
            notificationChannels = listOf(
                AlarmeeNotificationChannel(
                    id = "dailyNewsChannelId",
                    name = "Daily news notifications",
                    importance = NotificationManager.IMPORTANCE_HIGH,
                ),
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
