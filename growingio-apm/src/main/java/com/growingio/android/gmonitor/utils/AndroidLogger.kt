package com.growingio.android.gmonitor.utils

import android.util.Log

/**
 * <p>
 *
 * @author cpacm 2022/9/1
 */

const val TAG = "GMonitor"

fun Log.d(msg: String, err: Throwable? = null) {
    if (err != null) {
        Log.d(TAG, msg, err)
    } else {
        Log.d(TAG, msg)
    }
}

fun Log.e(msg: String, err: Throwable? = null) {
    if (err != null) {
        Log.e(TAG, msg, err)
    } else {
        Log.e(TAG, msg)
    }
}

class AndroidLogger : ILogger {
    override fun log(priority: Int, message: String, vararg args: Any) {
        log(priority, null, message, args)
    }

    override fun log(priority: Int, message: String, throwable: Throwable) {
        log(priority, throwable, message)
    }

    override fun log(priority: Int, throwable: Throwable?, message: String, vararg args: Any) {
        if (!isEnabled(priority)) return
        when (priority) {
            Log.INFO -> Log.i(TAG, message.format(args), throwable)
            Log.DEBUG -> Log.d(TAG, message.format(args), throwable)
            Log.ERROR -> Log.e(TAG, message.format(args), throwable)
            Log.WARN -> Log.w(TAG, message.format(args), throwable)
            else -> Log.d(TAG, message.format(args), throwable)
        }
    }

    override fun isEnabled(priority: Int): Boolean {
        return true
    }
}
