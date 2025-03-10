package com.growingio.android.gmonitor.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.growingio.android.gmonitor.ITracker
import com.growingio.android.gmonitor.Operation
import com.growingio.android.gmonitor.TrackerProvider
import com.growingio.android.gmonitor.event.Breadcrumb
import com.growingio.android.gmonitor.event.Breadcrumb.Companion.CATEGORY_PERFORMANCE_FRAGMENT
import java.util.*

@Deprecated("Use the Jetpack Fragment Library androidx.fragment.app.Fragment instead.")
class FragmentSupportLifecycleCallbacks(private val tracker: ITracker = TrackerProvider.instance) :
    FragmentManager.FragmentLifecycleCallbacks() {

    private val fragmentWithRunningOperation = WeakHashMap<Fragment, Operation>()

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        /**
         * When fragment putted to backstack,isAdded is false. remove judge
         * if (f.isAdded) {
         *    startTracing(f)
         * }
         */
        startTracing(f)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        stopTracing(f)
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        removeTracing(f)
    }

    private fun startTracing(fragment: Fragment) {
        if (fragmentWithRunningOperation.containsKey(fragment)) {
            return
        }
        val fragmentOp = Operation(CATEGORY_PERFORMANCE_FRAGMENT)

        val fragmentName = fragment.javaClass.simpleName
        fragmentOp.putData(Breadcrumb.ATTR_PERFORMANCE_PAGE_NAME, fragmentName)
        fragmentOp.putData(Breadcrumb.ATTR_PERFORMANCE_PAGE_FULLNAME, fragment.javaClass.name)
        fragmentOp.putData(
            Breadcrumb.ATTR_PERFORMANCE_LASTPAGE_FULLNAME,
            fragment.activity?.javaClass?.name ?: "undefined",
        )
        fragmentWithRunningOperation.put(fragment, fragmentOp)
    }

    private fun stopTracing(fragment: Fragment) {
        if (!fragmentWithRunningOperation.containsKey(fragment)) {
            return
        }

        val op = fragmentWithRunningOperation[fragment]
        op?.let {
            it.putData(Breadcrumb.ATTR_PERFORMANCE_PAGE_NAME, fragment.javaClass.simpleName)
            it.finish()
            tracker.trackBreadcrumb(Breadcrumb.pref(it))
            fragmentWithRunningOperation.remove(fragment)
        }
    }

    private fun removeTracing(fragment: Fragment) {
        if (!fragmentWithRunningOperation.containsKey(fragment)) {
            return
        }

        val op = fragmentWithRunningOperation[fragment]
        op?.let {
            it.putData(Breadcrumb.ATTR_PERFORMANCE_PAGE_NAME, fragment.javaClass.simpleName)
            it.finish()
            fragmentWithRunningOperation.remove(fragment)
        }
    }
}
