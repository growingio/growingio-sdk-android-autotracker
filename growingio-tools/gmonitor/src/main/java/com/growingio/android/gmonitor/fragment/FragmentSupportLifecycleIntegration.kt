package com.growingio.android.gmonitor.fragment

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.growingio.android.gmonitor.GMonitorOption
import com.growingio.android.gmonitor.ITracker
import com.growingio.android.gmonitor.Integration
import com.growingio.android.gmonitor.utils.ClassLoaderHelper

/**
 * <p>
 *     Android Support Fragment Lifecycle
 * @author cpacm 2022/9/26
 */
class FragmentSupportLifecycleIntegration(val application: Application) :
    Integration,
    Application.ActivityLifecycleCallbacks {

    private lateinit var tracker: ITracker
    private lateinit var options: GMonitorOption

    override fun register(tracker: ITracker, option: GMonitorOption) {
        this.tracker = tracker
        this.options = option

        val isFragmentAvailable = (
            options.enableFragmentSupportLifecycleTracing &&
                ClassLoaderHelper.isClassAvailable(FRAGMENT_SUPPORT_CLASS_NAME, options)
            )

        if (isFragmentAvailable) {
            application.registerActivityLifecycleCallbacks(this)
            option.logger.log(Log.DEBUG, "FragmentSupportLifecycleIntegration installed.")
        } else {
            option.logger.log(Log.DEBUG, "FragmentSupportLifecycleIntegration doesn't enabled.")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
            FragmentSupportLifecycleCallbacks(this.tracker),
            true,
        )
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
        options.logger.log(Log.DEBUG, "FragmentSupportLifecycleIntegration removed.")
    }

    companion object {
        private const val FRAGMENT_SUPPORT_CLASS_NAME = "android.support.v4.app.FragmentManager\$FragmentLifecycleCallbacks"
    }
}
