package com.growingio.android.gmonitor

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.util.Log
import com.growingio.android.gmonitor.event.Breadcrumb
import com.growingio.android.gmonitor.event.Breadcrumb.Companion.ATTR_PERFORMANCE_APP_COLD
import com.growingio.android.gmonitor.event.Breadcrumb.Companion.ATTR_PERFORMANCE_APP_DURATION
import com.growingio.android.gmonitor.event.Breadcrumb.Companion.ATTR_PERFORMANCE_LASTPAGE_FULLNAME
import com.growingio.android.gmonitor.event.Breadcrumb.Companion.ATTR_PERFORMANCE_PAGE_FULLNAME
import com.growingio.android.gmonitor.event.Breadcrumb.Companion.ATTR_PERFORMANCE_PAGE_NAME
import com.growingio.android.gmonitor.event.Breadcrumb.Companion.CATEGORY_PERFORMANCE_ACTIVITY
import com.growingio.android.gmonitor.event.Breadcrumb.Companion.CATEGORY_PERFORMANCE_APP
import java.io.Closeable
import java.util.*
import kotlin.math.max

/**
 * <p>
 *
 * @author cpacm 2022/9/2
 */
class ActivityLifecycleIntegration(val application: Application) :
    Integration,
    Application.ActivityLifecycleCallbacks,
    Closeable {

    private lateinit var tracker: ITracker
    private lateinit var options: GMonitorOption

    private var foregroundImportance = false
    private var firstActivityCreated = false
    private var firstActivityResumed = false
    private var activityCount = 0
    private val pageStack = Stack<String>()

    private var appStartOp: Operation? = null
    private val activityWithRunningOperation = WeakHashMap<Activity, Operation>()

    override fun register(tracker: ITracker, option: GMonitorOption) {
        this.tracker = tracker
        this.options = option

        foregroundImportance = isForegroundImportance(application)
        pageStack.push(application.javaClass.name)
        if (option.enableActivityLifecycleTracing) {
            application.registerActivityLifecycleCallbacks(this)
            this.options.logger.log(Log.DEBUG, "ActivityLifecycleIntegration is registered.")
        }
    }

    override fun close() {
        application.unregisterActivityLifecycleCallbacks(this)
        this.options.logger.log(Log.DEBUG, "ActivityLifecycleIntegration removed .")
    }

    private fun isRunningOperation(activity: Activity): Boolean {
        return activityWithRunningOperation.containsKey(activity)
    }

    private fun startOperation(activity: Activity) {
        if (!isRunningOperation(activity)) {
            val appStartTime = AppStartState.instance.getAppStartTime()

            appStartOp = Operation(CATEGORY_PERFORMANCE_APP, appStartTime)

            val activityOp = Operation(CATEGORY_PERFORMANCE_ACTIVITY)
            activityOp.putData(ATTR_PERFORMANCE_LASTPAGE_FULLNAME, pageStack.peek())
            activityOp.putData(ATTR_PERFORMANCE_PAGE_FULLNAME, activity.javaClass.name)
            activityWithRunningOperation.put(activity, activityOp)
        }
        pageStack.push(activity.javaClass.name)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // set cold start
        if (!firstActivityCreated) {
            // if Activity has savedInstanceState then its a warm start
            // https://developer.android.com/topic/performance/vitals/launch-time#warm
            AppStartState.instance.setColdStart(savedInstanceState == null)
        }

        startOperation(activity)

        firstActivityCreated = true
    }

    override fun onActivityStarted(activity: Activity) {
        activityCount += 1
        if (firstActivityResumed && activityCount == 1) {
            val op = activityWithRunningOperation.get(activity)
            if (op == null) {
                val activityOp = Operation(CATEGORY_PERFORMANCE_ACTIVITY)
                activityOp.putData(ATTR_PERFORMANCE_APP_COLD, false)
                activityOp.putData(ATTR_PERFORMANCE_PAGE_FULLNAME, activity.javaClass.name)
                activityWithRunningOperation.put(activity, activityOp)
            } else /*if (appStartOp?.isFinished() == false)*/ {
                op.putData(ATTR_PERFORMANCE_APP_COLD, false)
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        val op = activityWithRunningOperation.get(activity)

        if (!firstActivityResumed) {
            if (foregroundImportance) {
                AppStartState.instance.setAppEndTime()
            } else {
                options.logger.log(
                    Log.DEBUG,
                    "App Start won't be reported because Process wasn't of foregroundImportance.",
                )
            }

            appStartOp?.let {
                it.putData(ATTR_PERFORMANCE_APP_COLD, AppStartState.instance.coldStart ?: false)
                it.finish()

                op?.putData(ATTR_PERFORMANCE_APP_COLD, AppStartState.instance.coldStart ?: false)
                op?.putData(ATTR_PERFORMANCE_APP_DURATION, it.getDuration())

                val breadcrumb = Breadcrumb(Breadcrumb.TYPE_PERFORMANCE, it.name, it.description)
                    .putData(ATTR_PERFORMANCE_APP_DURATION, it.getDuration())
                    .putAllData(it.data)
                tracker.trackBreadcrumb(breadcrumb)
            }
            firstActivityResumed = true
        }

        op?.let {
            it.putData(ATTR_PERFORMANCE_PAGE_NAME, activity.javaClass.simpleName)
            it.finish()
            tracker.trackBreadcrumb(Breadcrumb.pref(it))
        }
        activityWithRunningOperation.remove(activity)
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        activityCount = max(0, activityCount - 1)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        // in case the appStartSpan isn't completed yet, we finish it as cancelled to avoid
        // memory leak
        if (appStartOp != null && appStartOp?.isFinished() == false) {
            appStartOp?.finish()
        }
        appStartOp = null

        val removeIndex = pageStack.lastIndexOf(activity.javaClass.name)
        if (removeIndex >= 0 && removeIndex < pageStack.size) {
            pageStack.removeAt(removeIndex)
        }
    }

    /**
     * Check if the Started process has IMPORTANCE_FOREGROUND importance which means that the process
     * will start an Activity.
     *
     * @return true if IMPORTANCE_FOREGROUND and false otherwise
     */
    private fun isForegroundImportance(context: Context): Boolean {
        if (options.avoidRunningAppProcesses) return true
        try {
            val service = context.getSystemService(Context.ACTIVITY_SERVICE)
            if (service is ActivityManager) {
                val runningAppProcesses = service.runningAppProcesses
                if (runningAppProcesses != null) {
                    val myPid = Process.myPid()
                    for (processInfo in runningAppProcesses) {
                        if (processInfo.pid == myPid) {
                            if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                return true
                            }
                            break
                        }
                    }
                }
            }
        } catch (ignored: SecurityException) {
            // happens for isolated processes
        } catch (ignored: Throwable) {
            // should never happen
        }
        return false
    }
}
