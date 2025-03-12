package com.growingio.android.gmonitor

import com.growingio.android.gmonitor.utils.ILogger
import com.growingio.android.gmonitor.utils.NoOpLogger
import java.util.concurrent.CopyOnWriteArrayList

/**
 * <p>
 *
 * @author cpacm 2022/9/1
 */
class GMonitorOption {

    var debug = false

    var enableActivityLifecycleTracing = true

    var enableFragmentXLifecycleTracing = true
    var enableFragmentSupportLifecycleTracing = false
    var enableFragmentSystemLifecycleTracing = false

    var enableUncaughtExceptionHandler = true

    var enableAnr = true

    var printUncaughtStackTrace = false

    var logger: ILogger = NoOpLogger()

    val integrations = CopyOnWriteArrayList<Integration>()

    var avoidRunningAppProcesses = false

    var anrTimeoutIntervalMillis = 5000L

    var anrInDebug = false
}
