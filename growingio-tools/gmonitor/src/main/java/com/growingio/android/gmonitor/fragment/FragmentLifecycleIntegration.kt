package com.growingio.android.gmonitor.fragment

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.growingio.android.gmonitor.GMonitorOption
import com.growingio.android.gmonitor.ITracker
import com.growingio.android.gmonitor.Integration
import com.growingio.android.gmonitor.utils.ClassLoaderHelper

/**
 * <p>
 *     Android System Fragment Lifecycle
 * @author cpacm 2022/9/26
 */
class FragmentLifecycleIntegration(val application: Application) :
    Integration,
    Application.ActivityLifecycleCallbacks {

    private lateinit var tracker: ITracker
    private lateinit var options: GMonitorOption

    override fun register(tracker: ITracker, option: GMonitorOption) {
        this.tracker = tracker
        this.options = option

        val isFragmentAvailable = (
            options.enableFragmentSystemLifecycleTracing &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                ClassLoaderHelper.isClassAvailable(FRAGMENT_CLASS_NAME, options)
            )

        if (isFragmentAvailable) {
            application.registerActivityLifecycleCallbacks(this)
            option.logger.log(Log.DEBUG, "FragmentLifecycleIntegration installed.")
        } else {
            option.logger.log(Log.DEBUG, "FragmentLifecycleIntegration doesn't enabled.")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.fragmentManager?.registerFragmentLifecycleCallbacks(
                FragmentLifecycleCallbacks(this.tracker),
                true,
            )
        }
    }

    override fun onActivityStarted(activity: Activity) { // no-op
    }

    override fun onActivityResumed(activity: Activity) { // no-op
    }

    override fun onActivityPaused(activity: Activity) { // no-op
    }

    override fun onActivityStopped(activity: Activity) { // no-op
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { // no-op
    }

    override fun onActivityDestroyed(activity: Activity) { // no-op
    }

    override fun close() {
        application.unregisterActivityLifecycleCallbacks(this)
        options.logger.log(Log.DEBUG, "FragmentLifecycleIntegration removed.")
    }

    companion object {
        private const val FRAGMENT_CLASS_NAME = "android.app.FragmentManager\$FragmentLifecycleCallbacks"
    }
}
