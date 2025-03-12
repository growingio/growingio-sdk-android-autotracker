package com.growingio.android.gmonitor.anr

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.growingio.android.gmonitor.GMonitorOption
import com.growingio.android.gmonitor.ITracker
import com.growingio.android.gmonitor.Integration
import com.growingio.android.gmonitor.event.Breadcrumb

/**
 * <p>
 *
 * @author cpacm 2022/9/9
 */
class AnrIntegration(val context: Context) : Integration, AnrReportListener {

    private lateinit var tracker: ITracker
    private lateinit var options: GMonitorOption

    override fun register(tracker: ITracker, option: GMonitorOption) {
        this.tracker = tracker
        this.options = option

        option.logger.log(Log.DEBUG, "AnrIntegration enabled: %s", options.enableAnr)
        if (options.enableAnr) {
            synchronized(watchDogLock) {
                if (anrWatchDog == null) {
                    anrWatchDog = AnrWatchDog(
                        context,
                        options.anrTimeoutIntervalMillis,
                        options.anrInDebug,
                        options.logger,
                        onAppNotResponding = this,
                    )
                    anrWatchDog!!.start()
                } else {
                    anrWatchDog!!.addAppNotResponding(this)
                }
            }
            options.logger.log(Log.DEBUG, "AnrIntegration installed.")
        }
    }

    override fun reportAnr(error: ApplicationNotRespondingException) {
        tracker.trackBreadcrumb(Breadcrumb.anr(error, error.stackTrace))
    }

    override fun close() {
        synchronized(watchDogLock) {
            if (anrWatchDog != null) {
                anrWatchDog!!.removeAppNotResponding(this)
                if (anrWatchDog!!.getAnrListenerCount() == 0) {
                    anrWatchDog!!.interrupt()
                    anrWatchDog = null
                    options.logger.log(Log.DEBUG, "AnrIntegration removed.")
                }
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var anrWatchDog: AnrWatchDog? = null

        val watchDogLock = Any()
    }
}
