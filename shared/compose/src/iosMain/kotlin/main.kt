import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.sedsoftware.blinkly.component.root.RootComponent
import com.sedsoftware.blinkly.component.root.RootComponentFactory
import com.sedsoftware.blinkly.compose.ui.RootContent
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType
import dev.icerock.moko.permissions.ios.PermissionsController
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIViewController
import platform.UIKit.setStatusBarStyle

private val lifecycle: LifecycleRegistry by lazy {
    LifecycleRegistry()
}

private val permissionsController: PermissionsController by lazy {
    PermissionsController()
}

private val rootComponent: RootComponent by lazy {
    RootComponentFactory(
        componentContext = DefaultComponentContext(lifecycle),
        contentConfigurations = getNotificationConfigurations(),
        permissionsController = permissionsController,
    )

}

@Suppress("FunctionNaming")
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        RootContent(
            component = rootComponent,
            onThemeChanged = { ThemeChanged(it) },
        )
    }
}

@Composable
private fun ThemeChanged(isDark: Boolean) {
    LaunchedEffect(isDark) {
        UIApplication.sharedApplication.setStatusBarStyle(
            if (isDark) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent
        )
    }
}

private fun getNotificationConfigurations(): Map<ReminderType, ReminderConfig> {
    return mapOf(
        ReminderType.TWENTY_X3 to ReminderConfig(
            title = "20-20-20",
            description = "Please take a break",
        )
    )
}
