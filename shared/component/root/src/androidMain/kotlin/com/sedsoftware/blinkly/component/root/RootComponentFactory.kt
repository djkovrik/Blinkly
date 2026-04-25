package com.sedsoftware.blinkly.component.root

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.russhwolf.settings.Settings
import com.sedsoftware.blinkly.alarm.di.AlarmModule
import com.sedsoftware.blinkly.alarm.di.AlarmModuleDependencies
import com.sedsoftware.blinkly.component.root.integration.RootComponentDefault
import com.sedsoftware.blinkly.database.BlinklyDatabaseDriverFactory
import com.sedsoftware.blinkly.database.di.DatabaseModule
import com.sedsoftware.blinkly.database.di.DatabaseModuleDependencies
import com.sedsoftware.blinkly.domain.di.DomainModule
import com.sedsoftware.blinkly.domain.di.DomainModuleDependencies
import com.sedsoftware.blinkly.domain.external.BlinklyAlarmManager
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType
import com.sedsoftware.blinkly.notifier.di.NotifierModule
import com.sedsoftware.blinkly.notifier.di.NotifierModuleDependencies
import com.sedsoftware.blinkly.settings.SharedSettingsFactory
import com.sedsoftware.blinkly.settings.di.SettingsModule
import com.sedsoftware.blinkly.settings.di.SettingsModuleDependencies
import com.sedsoftware.blinkly.utils.di.UtilsModule
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Suppress("FunctionName")
fun RootComponentFactory(
    componentContext: ComponentContext,
    contentConfigurations: Map<ReminderType, ReminderConfig>,
    permissionsController: PermissionsController,
    context: Context,
): RootComponent {

    val dispatchers: BlinklyDispatchers by lazy {
        object : BlinklyDispatchers {
            override val main: CoroutineDispatcher = Dispatchers.Main.immediate
            override val io: CoroutineDispatcher = Dispatchers.IO
        }
    }

    val timeUtils: BlinklyTimeUtils by lazy {
        val utilsModule = UtilsModule()
        utilsModule.timeUtils
    }

    val alarmManager: BlinklyAlarmManager by lazy {
        val alarmModule = AlarmModule(
            dependencies = object : AlarmModuleDependencies {
                override val timeUtils: BlinklyTimeUtils = timeUtils
                override val contentConfigurations: Map<ReminderType, ReminderConfig> = contentConfigurations
            }
        )

        alarmModule.alarmManager
    }

    val database: BlinklyDatabase by lazy {
        val databaseModule = DatabaseModule(
            dependencies = object : DatabaseModuleDependencies {
                override val driver: SqlDriver = BlinklyDatabaseDriverFactory(context)
                override val dispatchers: BlinklyDispatchers = dispatchers
                override val timeUtils: BlinklyTimeUtils = timeUtils
            }
        )

        databaseModule.database
    }

    val notifier: BlinklyNotifier by lazy {
        val notifierModule = NotifierModule(
            dependencies = object : NotifierModuleDependencies {
                override val controller: PermissionsController = permissionsController
            }
        )

        notifierModule.notifier
    }

    val settings: BlinklySettings by lazy {
        val settingsModule = SettingsModule(
            dependencies = object : SettingsModuleDependencies {
                override val settings: Settings = SharedSettingsFactory(context)
            }
        )

        settingsModule.settings
    }

    val domainModule: DomainModule by lazy {
        DomainModule(
            dependencies = object : DomainModuleDependencies {
                override val alarmManager: BlinklyAlarmManager = alarmManager
                override val database: BlinklyDatabase = database
                override val notifier: BlinklyNotifier = notifier
                override val settings: BlinklySettings = settings
                override val timeUtils: BlinklyTimeUtils = timeUtils
                override val dispatchers: BlinklyDispatchers = dispatchers
            }
        )
    }

    return RootComponentDefault(
        componentContext = componentContext,
        storeFactory = DefaultStoreFactory(),
        alarmManager = alarmManager,
        database = database,
        dispatchers = dispatchers,
        notifier = notifier,
        settings = settings,
        timeUtils = timeUtils,
        achievementsWatcher = domainModule.achievementsWatcher,
        calendarWatcher = domainModule.calendarWatcher,
        exerciseManager = domainModule.exerciseManager,
        highlightsProvider = domainModule.highlightsProvider,
        reminderManager = domainModule.reminderManager,
        treeProgressWatcher = domainModule.treeProgressWatcher,
    )
}
