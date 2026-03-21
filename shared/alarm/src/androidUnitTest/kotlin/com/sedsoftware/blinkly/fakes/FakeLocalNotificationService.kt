package com.sedsoftware.blinkly.fakes

import com.tweener.alarmee.LocalNotificationService
import com.tweener.alarmee.model.Alarmee

class FakeLocalNotificationService : LocalNotificationService {

    private val scheduled: MutableMap<String, Alarmee> = mutableMapOf()
    private val immediate: MutableMap<String, Alarmee> = mutableMapOf()

    fun currentAlarms(): Map<String, Alarmee> = scheduled

    fun clearScheduled() = scheduled.clear()

    override fun schedule(alarmee: Alarmee) {
        scheduled[alarmee.uuid] = alarmee
    }

    override fun immediate(alarmee: Alarmee) {
        immediate[alarmee.uuid] = alarmee
    }

    override fun cancel(uuid: String) {
        scheduled.remove(uuid)
    }

    override fun cancelAll() {
        scheduled.clear()
    }
}
