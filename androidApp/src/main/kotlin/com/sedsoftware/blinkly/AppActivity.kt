package com.sedsoftware.blinkly

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        val splashScreen = installSplashScreen()

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.iconView.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .withEndAction { splashScreenView.remove() }
                .start()
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        permissionsController.bind(this)

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
        val texts = getRandomNotificationTexts()
        return mapOf(
            ReminderType.TWENTY_X3 to ReminderConfig(
                title = resources.getString(texts.first),
                description = resources.getString(texts.second),
            )
        )
    }

    private fun getRandomNotificationTexts(): Pair<Int, Int> {
        val list = listOf(
            R.string.notification_title1 to R.string.notification_description1,
            R.string.notification_title2 to R.string.notification_description2,
            R.string.notification_title3 to R.string.notification_description3,
            R.string.notification_title4 to R.string.notification_description4,
            R.string.notification_title5 to R.string.notification_description5,
            R.string.notification_title6 to R.string.notification_description6,
            R.string.notification_title7 to R.string.notification_description7,
            R.string.notification_title8 to R.string.notification_description8,
            R.string.notification_title9 to R.string.notification_description9,
            R.string.notification_title10 to R.string.notification_description10,
        )

        return list.random()
    }

    private companion object {
        const val ANIMATION_DURATION = 250L
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
