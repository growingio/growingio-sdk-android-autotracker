package com.growingio.android.gmonitor

import android.util.Log
import com.growingio.android.gmonitor.event.Breadcrumb

/**
 * <p>
 *
 * @author cpacm 2022/9/2
 */
class GMonitorTracker : ITracker {

    override fun trackBreadcrumb(breadcrumb: Breadcrumb) {
        Log.d("Tracker", breadcrumb.toString())
    }

    override fun clone(): GMonitorTracker = GMonitorTracker()
}

class TrackerProvider : ITracker {
    override fun trackBreadcrumb(breadcrumb: Breadcrumb) {
        GMonitor.getInstance()?.trackBreadcrumb(breadcrumb)
    }

    override fun clone(): ITracker = this

    companion object {
        val instance = TrackerProvider()
    }
}
