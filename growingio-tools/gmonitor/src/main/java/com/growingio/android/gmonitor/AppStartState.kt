package com.growingio.android.gmonitor

import android.os.SystemClock

/**
 * <p>
 *
 * @author cpacm 2022/9/1
 */
internal class AppStartState private constructor() {

    private var appStartMills = 0L
    private var appEndMills = 0L
    var coldStart: Boolean? = null

    @Synchronized
    fun setAppStartTime(mills: Long) {
        if (appStartMills != 0L) return
        this.appStartMills = mills
    }

    @Synchronized
    fun setAppStartTime() {
        if (appStartMills != 0L) return
        this.appStartMills = SystemClock.uptimeMillis()
    }

    @Synchronized
    fun getAppStartTime(): Long = appStartMills

    @Synchronized
    fun setAppEndTime() {
        appEndMills = SystemClock.uptimeMillis()
    }

    @Synchronized
    fun setColdStart(cold: Boolean) {
        if (coldStart != null) return
        this.coldStart = cold
    }

    @Synchronized
    fun getAppStartInterval(): Long? {
        if (appStartMills == 0L || appEndMills == 0L || coldStart == null) {
            return null
        }
        val appInterval = appEndMills - appStartMills

        // it's too long,just drop it.
        if (appInterval >= 60000) {
            return null
        }
        return appInterval
    }

    companion object {
        val instance = AppStartState()
    }
}
