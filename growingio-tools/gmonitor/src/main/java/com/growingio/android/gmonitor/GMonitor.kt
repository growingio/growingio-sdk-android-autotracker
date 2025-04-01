package com.growingio.android.gmonitor

import android.app.Application
import android.content.Context
import android.os.SystemClock
import com.growingio.android.gmonitor.anr.AnrIntegration
import com.growingio.android.gmonitor.crash.UncaughtExceptionHandlerIntegration
import com.growingio.android.gmonitor.event.Breadcrumb
import com.growingio.android.gmonitor.fragment.FragmentLifecycleIntegration
import com.growingio.android.gmonitor.fragment.FragmentSupportLifecycleIntegration
import com.growingio.android.gmonitor.fragment.FragmentXLifecycleIntegration
import com.growingio.android.gmonitor.utils.AndroidLogger
import com.growingio.android.gmonitor.utils.ILogger

/**
 * <p>
 *
 * @author cpacm 2022/9/1
 */
class GMonitor private constructor(val option: GMonitorOption, val tracker: ITracker = GMonitorTracker()) {

    private val currentTracker = ThreadLocal<ITracker>()

    init {
        currentTracker.set(tracker)

        option.integrations.forEach {
            it.register(getCurrentTracker(), option)
        }
    }

    private fun getCurrentTracker(): ITracker {
        var tracker = currentTracker.get()
        if (tracker == null) {
            tracker = this.tracker.clone()
            currentTracker.set(tracker)
        }
        return tracker
    }

    fun trackBreadcrumb(builder: Breadcrumb) {
        getCurrentTracker().trackBreadcrumb(builder)
    }

    fun close() {
        option.integrations.forEach {
            it.close()
        }
    }

    companion object {

        @Volatile
        private var gMonitor: GMonitor? = null

        @JvmStatic
        fun getInstance(): GMonitor? {
            return gMonitor
        }

        private val appStart = SystemClock.uptimeMillis()

        @JvmStatic
        fun appStart() {
            AppStartState.instance.setAppStartTime(appStart)
        }

        @JvmStatic
        fun init(
            context: Context,
            logger: ILogger = AndroidLogger(),
            option: GMonitorOption = GMonitorOption(),
            tracker: ITracker = GMonitorTracker(),
        ) {
            // when sdk init,set app start time
            AppStartState.instance.setAppStartTime(appStart)

            option.logger = logger
            if (context is Application) {
                option.integrations.add(ActivityLifecycleIntegration(context))
                option.integrations.add(FragmentXLifecycleIntegration(context))
                option.integrations.add(FragmentSupportLifecycleIntegration(context))
                option.integrations.add(FragmentLifecycleIntegration(context))
            }
            option.integrations.add(UncaughtExceptionHandlerIntegration())
            option.integrations.add(AnrIntegration(context))

            gMonitor = GMonitor(option, tracker)
        }
    }
}
