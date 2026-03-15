package com.sedsoftware.blinkly.notifier.di

import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.notifier.impl.BlinklyNotifierImpl
import com.sedsoftware.blinkly.notifier.impl.PermissionServiceImpl
import dev.icerock.moko.permissions.PermissionsController

interface NotifierModule {
    val settings: BlinklyNotifier
}

interface NotifierModuleDependencies {
    val controller: PermissionsController
}

fun NotifierModule(dependencies: NotifierModuleDependencies): NotifierModule {
    return object : NotifierModule {
        override val settings: BlinklyNotifier by lazy {
            BlinklyNotifierImpl(
                permissionsService = PermissionServiceImpl(
                    controller = dependencies.controller,
                )
            )
        }
    }
}
