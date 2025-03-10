package com.growingio.android.gmonitor.crash

import android.util.Log
import com.growingio.android.gmonitor.GMonitorOption
import com.growingio.android.gmonitor.ITracker
import com.growingio.android.gmonitor.Integration
import com.growingio.android.gmonitor.event.Breadcrumb

/**
 * <p>
 *
 * @author cpacm 2022/9/6
 */
class UncaughtExceptionHandlerIntegration : Integration, Thread.UncaughtExceptionHandler {

    private lateinit var tracker: ITracker
    private lateinit var options: GMonitorOption

    private val threadAdapter: UncaughtExceptionHandler
    private var lastExceptionHandler: Thread.UncaughtExceptionHandler? = null

    init {
        threadAdapter = UncaughtExceptionHandler.Adapter.instance
    }

    override fun register(tracker: ITracker, option: GMonitorOption) {
        this.tracker = tracker
        this.options = option

        if (options.enableUncaughtExceptionHandler) {
            val currentHandler = threadAdapter.getDefaultUncaughtExceptionHandler()
            if (currentHandler != null) {
                this.options.logger.log(
                    Log.DEBUG,
                    "default UncaughtExceptionHandler class=${currentHandler.javaClass.name}'",
                )
                lastExceptionHandler = currentHandler
            }
            threadAdapter.setDefaultUncaughtExceptionHandler(this)
        }
        this.options.logger.log(Log.DEBUG, "UncaughtExceptionHandlerIntegration installed.")
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        options.logger.log(Log.INFO, "Uncaught exception received.")
        try {
            val timeMillis = System.currentTimeMillis()
            val title = e.message
            // TODO track sendException, need process detail
            options.logger.log(Log.ERROR, "uncaughtException", e)
            tracker.trackBreadcrumb(Breadcrumb.error(e = e))
            // FileUtils.setAppendFile(errorLog)
        } catch (e: Exception) {
            options.logger.log(Log.ERROR, "Error sending uncaught exception to Sentry. ", e)
        }
        if (lastExceptionHandler != null) {
            options.logger.log(Log.INFO, "Invoking inner uncaught exception handler.")
            lastExceptionHandler?.uncaughtException(t, e)
        } else if (options.printUncaughtStackTrace) {
            e.printStackTrace()
        }
    }

    override fun close() {
        if (this == threadAdapter.getDefaultUncaughtExceptionHandler()) {
            threadAdapter.setDefaultUncaughtExceptionHandler(lastExceptionHandler)
            options.logger.log(Log.DEBUG, "UncaughtExceptionHandlerIntegration removed.")
        }
    }
}

interface UncaughtExceptionHandler {

    fun getDefaultUncaughtExceptionHandler(): Thread.UncaughtExceptionHandler?

    fun setDefaultUncaughtExceptionHandler(handler: Thread.UncaughtExceptionHandler?)

    // ex handler for outside api
    class Adapter private constructor() : UncaughtExceptionHandler {

        companion object {
            val instance: Adapter = Adapter()
        }

        override fun getDefaultUncaughtExceptionHandler(): Thread.UncaughtExceptionHandler? {
            return Thread.getDefaultUncaughtExceptionHandler()
        }

        override fun setDefaultUncaughtExceptionHandler(handler: Thread.UncaughtExceptionHandler?) {
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }
    }
}
