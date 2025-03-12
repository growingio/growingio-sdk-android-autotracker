package com.growingio.android.gmonitor

import java.io.Closeable

/**
 * <p>
 *     placeholder for module design
 * @author cpacm 2022/9/2
 */
interface Integration : Closeable {
    fun register(tracker: ITracker, option: GMonitorOption)
}
