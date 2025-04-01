package com.growingio.android.gmonitor.fragment

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.growingio.android.gmonitor.GMonitorOption
import com.growingio.android.gmonitor.ITracker
import com.growingio.android.gmonitor.Integration
import com.growingio.android.gmonitor.utils.ClassLoaderHelper

/**
 * <p>
 *     Android Fragment X Lifecycle
 * @author cpacm 2022/9/6
 */
class FragmentXLifecycleIntegration(val application: Application) :
    Integration,
    Application.ActivityLifecycleCallbacks {

    private lateinit var tracker: ITracker
    private lateinit var options: GMonitorOption

    override fun register(tracker: ITracker, option: GMonitorOption) {
        this.tracker = tracker
        this.options = option

        val isFragmentAvailable = (
            options.enableFragmentXLifecycleTracing &&
                ClassLoaderHelper.isClassAvailable(FRAGMENT_X_CLASS_NAME, options)
            )

        if (isFragmentAvailable) {
            application.registerActivityLifecycleCallbacks(this)
            option.logger.log(Log.DEBUG, "FragmentXLifecycleIntegration installed.")
        } else {
            option.logger.log(Log.DEBUG, "FragmentXLifecycleIntegration doesn't enabled.")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
            FragmentXLifecycleCallbacks(this.tracker),
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
        options.logger.log(Log.DEBUG, "FragmentLifecycleIntegration removed.")
    }

    companion object {
        private const val FRAGMENT_X_CLASS_NAME = "androidx.fragment.app.FragmentManager\$FragmentLifecycleCallbacks"
    }
}
