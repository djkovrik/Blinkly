package com.sedsoftware.blinkly.component.sync.di

import com.sedsoftware.blinkly.component.sync.auth.FirebaseBlinklyAuthService
import com.sedsoftware.blinkly.component.sync.domain.BlinklySyncManagerImpl
import com.sedsoftware.blinkly.component.sync.remote.FirestoreSyncDataSource
import com.sedsoftware.blinkly.component.sync.tracking.TrackingBlinklyDatabase
import com.sedsoftware.blinkly.component.sync.tracking.TrackingBlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyAuthService
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyRemoteSyncDataSource
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklySyncManager
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import kotlinx.coroutines.CoroutineScope

interface SyncModule {
    val authService: BlinklyAuthService
    val remoteSyncDataSource: BlinklyRemoteSyncDataSource
    val syncManager: BlinklySyncManager
    val trackedDatabase: BlinklyDatabase
    val trackedSettings: BlinklySettings
}

interface SyncModuleDependencies {
    val database: BlinklyDatabase
    val settings: BlinklySettings
    val dispatchers: BlinklyDispatchers
    val timeUtils: BlinklyTimeUtils
}

@Suppress("FunctionName")
fun SyncModule(dependencies: SyncModuleDependencies): SyncModule =
    object : SyncModule {
        override val authService: BlinklyAuthService by lazy {
            FirebaseBlinklyAuthService()
        }

        override val remoteSyncDataSource: BlinklyRemoteSyncDataSource by lazy {
            FirestoreSyncDataSource()
        }

        private val scope: CoroutineScope by lazy {
            CoroutineScope(dependencies.dispatchers.main)
        }

        override val syncManager: BlinklySyncManager by lazy {
            BlinklySyncManagerImpl(
                authService = authService,
                database = dependencies.database,
                settings = dependencies.settings,
                remoteDataSource = remoteSyncDataSource,
                timeUtils = dependencies.timeUtils,
                scope = scope,
            )
        }

        override val trackedDatabase: BlinklyDatabase by lazy {
            TrackingBlinklyDatabase(
                delegate = dependencies.database,
                settings = dependencies.settings,
                timeUtils = dependencies.timeUtils,
            )
        }

        override val trackedSettings: BlinklySettings by lazy {
            TrackingBlinklySettings(
                delegate = dependencies.settings,
                timeUtils = dependencies.timeUtils,
            )
        }
    }
