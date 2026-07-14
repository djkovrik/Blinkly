package com.sedsoftware.blinkly

import android.content.Context
import com.sedsoftware.blinkly.domain.model.ReminderConfig
import com.sedsoftware.blinkly.domain.model.ReminderType

internal fun Context.getNotificationConfigurations(): Map<ReminderType, ReminderConfig> {
    val texts = getRandomNotificationTexts()
    return mapOf(
        ReminderType.TWENTY_X3 to ReminderConfig(
            title = getString(texts.first),
            description = getString(texts.second),
        )
    )
}

private fun Context.getRandomNotificationTexts(): Pair<Int, Int> =
    listOf(
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
    ).random()
