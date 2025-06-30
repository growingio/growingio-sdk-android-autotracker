package com.growingio.android.gmonitor.anr

import android.app.ActivityManager
import android.app.ActivityManager.ProcessErrorStateInfo
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.growingio.android.gmonitor.utils.ILogger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * <p>
 *
 * @author cpacm 2022/9/9
 */
class AnrWatchDog constructor(
    private val context: Context,
    private val anrMills: Long,
    private val enableDebug: Boolean,
    private val logger: ILogger,
    private val uiHandler: Handler = Handler(Looper.getMainLooper()),
    private val onAppNotResponding: AnrReportListener? = null,
) : Thread() {

    private val tick = AtomicLong(0L)
    private val report = AtomicBoolean(false)
    private val anrListeners = arrayListOf<AnrReportListener>()

    init {
        onAppNotResponding?.let { addAppNotResponding(it) }
    }

    val ticker = Runnable {
        tick.set(0)
        report.set(false)
    }

    fun addAppNotResponding(listener: AnrReportListener) {
        if (!anrListeners.contains(listener)) {
            anrListeners.add(listener)
        }
    }

    fun removeAppNotResponding(listener: AnrReportListener) {
        anrListeners.remove(listener)
    }

    fun getAnrListenerCount(): Int = anrListeners.size

    override fun run() {
        name = "|GMonitor-ANR|"
        var interval = anrMills
        while (!isInterrupted) {
            val needTick = tick.get() == 0L
            tick.addAndGet(interval)
            if (needTick) {
                uiHandler.post(ticker)
            }

            try {
                sleep(interval)
            } catch (e: InterruptedException) {
                try {
                    Thread.currentThread().interrupt()
                } catch (e: SecurityException) {
                    logger.log(Log.WARN, "Fail to interrupt in ${currentThread().name} due to %s", e.message.toString())
                }
                logger.log(Log.WARN, "Interrupted: %s", e.message.toString())
                return
            }

            // Anr occur when tick has not set to 0
            if (tick.get() != 0L && !report.get()) {
                if (!enableDebug && (Debug.isDebuggerConnected() || Debug.waitingForDebugger())) {
                    logger.log(Log.WARN, "An ANR was detected but ignored because the debugger is connected.")
                    report.set(true)
                    continue
                }
            }

            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            var processErrorStateInfo: ProcessErrorStateInfo? = null
            if (am != null) {
                var processesInErrorState: List<ProcessErrorStateInfo?>? = null
                try {
                    // It can throw RuntimeException or OutOfMemoryError
                    processesInErrorState = am.getProcessesInErrorState()
                } catch (e: Throwable) {
                    logger.log(
                        Log.ERROR,
                        "Error getting ActivityManager#getProcessesInErrorState.",
                        e,
                    )
                }
                if (processesInErrorState == null) {
                    continue
                }

                var isAnr = false
                for (item in processesInErrorState) {
                    if (item?.condition == ProcessErrorStateInfo.NOT_RESPONDING) {
                        isAnr = true
                        processErrorStateInfo = item
                        break
                    }
                }
                if (!isAnr) {
                    continue
                }
            }

            logger.log(Log.INFO, "Raising ANR")
            var message = "Application Not Responding for at least $anrMills ms."
            var stackTrack = uiHandler.looper.thread.name
            if (processErrorStateInfo != null) {
                stackTrack = processErrorStateInfo.longMsg
                message += " Process[" + processErrorStateInfo.processName + "]"
            }

            val error = ApplicationNotRespondingException(message, stackTrack, uiHandler.looper.thread)
            anrListeners.forEach {
                it.reportAnr(error)
            }
            interval = anrMills
            report.set(true)
        }
    }
}
