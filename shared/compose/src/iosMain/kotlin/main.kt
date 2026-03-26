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
import platform.Foundation.NSBundle
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
    val texts = getRandomNotificationTexts()
    val title = NSBundle.mainBundle.localizedStringForKey(texts.first, "", null)
    val description = NSBundle.mainBundle.localizedStringForKey(texts.second, "", null)
    return mapOf(
        ReminderType.TWENTY_X3 to ReminderConfig(
            title = title,
            description = description,
        )
    )
}

private fun getRandomNotificationTexts(): Pair<String, String> {
    val list = listOf(
        "notification_title1" to "notification_description1",
        "notification_title2" to "notification_description2",
        "notification_title3" to "notification_description3",
        "notification_title4" to "notification_description4",
        "notification_title5" to "notification_description5",
        "notification_title6" to "notification_description6",
        "notification_title7" to "notification_description7",
        "notification_title8" to "notification_description8",
        "notification_title9" to "notification_description9",
        "notification_title10" to "notification_description10",
    )

    return list.random()
}
