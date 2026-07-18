package com.sedsoftware.blinkly

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.defaultComponentContext
import com.sedsoftware.blinkly.component.root.RootComponentFactory
import com.sedsoftware.blinkly.compose.auth.initializeBlinklyGoogleAuth
import com.sedsoftware.blinkly.compose.ads.BlinklyAdPlacement
import com.sedsoftware.blinkly.compose.ads.BlinklyAdsBuildType
import com.sedsoftware.blinkly.compose.ads.BlinklyAdsConfiguration
import com.sedsoftware.blinkly.compose.ads.BlinklyAdsPlatform
import com.sedsoftware.blinkly.compose.ui.RootContent
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

        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )

        permissionsController.bind(this)
        initializeBlinklyGoogleAuth(serverId = getString(R.string.default_web_client_id))

        val rootComponent = RootComponentFactory(
            componentContext = defaultComponentContext(),
            contentConfigurations = getNotificationConfigurations(),
            permissionsController = permissionsController,
            context = applicationContext,
        )

        setContent {
            RootContent(
                component = rootComponent,
                adsConfiguration = getAdsConfiguration(),
                onSystemBarsAppearanceChanged = { SystemBarsAppearanceChanged(it) },
            )
        }
    }

    private companion object {
        const val ANIMATION_DURATION = 250L
    }

    private fun getAdsConfiguration(): BlinklyAdsConfiguration =
        BlinklyAdsConfiguration(
            achievementsAdUnitId = BuildConfig.BLINKLY_ACHIEVEMENTS_AD_UNIT_ID,
            gardenAdUnitId = BuildConfig.BLINKLY_GARDEN_AD_UNIT_ID,
            enabledPlacements = BlinklyAdPlacement.entries.toSet(),
            privacyReady = true,
            platform = BlinklyAdsPlatform.ANDROID,
            buildType = if (BuildConfig.DEBUG) BlinklyAdsBuildType.DEBUG else BlinklyAdsBuildType.RELEASE,
            appVersion = BuildConfig.VERSION_NAME,
        )
}

@Composable
private fun SystemBarsAppearanceChanged(useDarkIcons: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = useDarkIcons
            isAppearanceLightNavigationBars = useDarkIcons
        }
    }
}
