package com.growingio.android.gmonitor

import android.os.SystemClock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * <p>
 *     we need an operation to record the continuation of a message
 * @author cpacm 2022/9/2
 */
class Operation(op: String, startTimestamp: Long? = null) {

    // the operation name
    val name: String = op

    var description: String? = null
        set(value) {
            if (finished.get()) return
            field = value
        }

    // the moment in time when operation start
    val startMills: Long

    var endMills: Long? = null

    val finished = AtomicBoolean(false)

    var throwable: Throwable? = null

    var status: OperationStatus = OperationStatus.OK

    val data = ConcurrentHashMap<String, Any>()

    init {
        this.startMills = startTimestamp ?: SystemClock.uptimeMillis()
    }

    fun putData(key: String, value: Any) {
        if (finished.get()) return
        data[key] = value
    }

    fun isFinished(): Boolean = finished.get()

    fun setStatus(status: OperationStatus, e: Throwable?) {
        this.status = status
        throwable = e
    }

    fun getDuration(): Long {
        if (!finished.get()) {
            return 0
        }
        return this.endMills!! - this.startMills
    }

    fun finish() {
        // the operation can be finished only once
        if (!finished.compareAndSet(false, true)) {
            return
        }
        this.endMills = SystemClock.uptimeMillis()
    }
}

sealed class OperationStatus {
    object OK : OperationStatus()
    object CANCELLED : OperationStatus()
    object ERROR : OperationStatus()
}
