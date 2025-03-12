package com.growingio.android.gmonitor.utils

/**
 * <p>
 *
 * @author cpacm 2022/9/1
 */
class NoOpLogger : ILogger {
    override fun log(priority: Int, message: String, vararg args: Any) {
    }

    override fun log(priority: Int, message: String, throwable: Throwable) {
    }

    override fun log(priority: Int, throwable: Throwable?, message: String, vararg args: Any) {
    }

    override fun isEnabled(priority: Int): Boolean {
        return false
    }
}
