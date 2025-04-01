package com.growingio.android.gmonitor.anr

/**
 * <p>
 *
 * @author cpacm 2022/9/9
 */
class ApplicationNotRespondingException(message: String, val stackTrace: String, val thread: Thread) : RuntimeException(message)

interface AnrReportListener {
    fun reportAnr(error: ApplicationNotRespondingException)
}
