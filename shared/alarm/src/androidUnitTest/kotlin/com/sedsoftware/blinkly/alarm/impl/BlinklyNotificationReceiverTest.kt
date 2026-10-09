package com.sedsoftware.blinkly.alarm.impl

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.os.Looper
import com.tweener.alarmee.reveicer.NotificationBroadcastReceiver
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], shadows = [FailingNotificationManager::class])
class BlinklyNotificationReceiverTest {

    @Test
    fun `factory intercepts existing Alarmee receiver and delegates unrelated receivers`() {
        val factory = BlinklyAlarmComponentFactory()
        assertIs<BlinklyNotificationReceiver>(factory.instantiateReceiverCompat(
            javaClass.classLoader!!, NotificationBroadcastReceiver::class.java.name, null,
        ))
        assertIs<UnrelatedReceiver>(factory.instantiateReceiverCompat(
            javaClass.classLoader!!, UnrelatedReceiver::class.java.name, null,
        ))
    }

    @Test
    fun `real receiver contains Binder failure and preserves the repeating PendingIntent`() {
        val context = RuntimeEnvironment.getApplication()
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(context.packageName)
        shadowOf(context.packageManager).addResolveInfoForIntent(launcher, ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                name = "TestLauncherActivity"
                packageName = context.packageName
                applicationInfo = context.applicationInfo
            }
        })
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val uuid = "test-reminder"
        val anchor = System.currentTimeMillis() + 60_000L
        val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = NotificationBroadcastReceiver.ALARM_ACTION
            putExtra(NotificationBroadcastReceiver.KEY_UUID, uuid)
            putExtra(NotificationBroadcastReceiver.KEY_TITLE, "Title")
            putExtra(NotificationBroadcastReceiver.KEY_BODY, "Body")
            putExtra(NotificationBroadcastReceiver.KEY_CHANNEL_ID, "test-channel")
            putExtra(NotificationBroadcastReceiver.KEY_REPEAT_INTERVAL_MILLIS, 60_000L)
            putExtra(NotificationBroadcastReceiver.KEY_NEXT_TRIGGER_MILLIS, anchor)
        }
        FailingNotificationManager.called = CountDownLatch(1)
        val receiver = BlinklyAlarmComponentFactory().instantiateReceiverCompat(
            javaClass.classLoader!!, NotificationBroadcastReceiver::class.java.name, intent,
        )
        context.registerReceiver(receiver, IntentFilter(intent.action), Context.RECEIVER_NOT_EXPORTED)
        context.sendBroadcast(Intent(intent).setComponent(null))
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(FailingNotificationManager.called.await(10, TimeUnit.SECONDS))
        assertTrue(shadowOf(receiver).wentAsync())
        shadowOf(shadowOf(receiver).originalPendingResult).future.get(10, TimeUnit.SECONDS)
        context.unregisterReceiver(receiver)
        val alarm = shadowOf(manager).scheduledAlarms.single()
        assertEquals(anchor, alarm.triggerAtTime)
        val nextIntent = shadowOf(alarm.operation).savedIntent
        assertEquals(intent.component, nextIntent.component)
        assertEquals(uuid, nextIntent.getStringExtra(NotificationBroadcastReceiver.KEY_UUID))
        assertEquals(anchor + 60_000L, nextIntent.getLongExtra(NotificationBroadcastReceiver.KEY_NEXT_TRIGGER_MILLIS, -1))
        // Alarmee cancels by the original component/action and UUID hash, without delivery extras.
        val cancelIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = NotificationBroadcastReceiver.ALARM_ACTION
        }
        manager.cancel(PendingIntent.getBroadcast(
            context, uuid.hashCode(), cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        ))
        assertTrue(shadowOf(manager).scheduledAlarms.isEmpty())
    }

    class UnrelatedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = Unit
    }
}

@Implements(NotificationManager::class)
class FailingNotificationManager {
    // Robolectric requires a method matching the Android API, even for a constant result.
    @Suppress("FunctionOnlyReturningConstant")
    @Implementation
    fun areNotificationsEnabled(): Boolean = true

    @Suppress("UNUSED_PARAMETER")
    @Implementation
    fun notify(id: Int, notification: Notification): Unit = throw NullPointerException("OEM Binder failure").also {
        called.countDown()
    }

    companion object {
        var called = CountDownLatch(1)
    }
}
