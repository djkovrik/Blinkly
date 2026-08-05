package com.sedsoftware.blinkly.domain.external

interface BlinklyCrashReporter {
    fun log(message: String)
    fun recordException(throwable: Throwable)
    fun setCustomKey(key: String, value: String)
    fun setUserId(userId: String)
}
