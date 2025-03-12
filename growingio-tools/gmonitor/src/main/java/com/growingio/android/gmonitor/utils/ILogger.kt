package com.growingio.android.gmonitor.utils

/**
 * <p>
 *
 * @author cpacm 2022/9/1
 */
interface ILogger {

    fun log(priority: Int, message: String, vararg args: Any)

    fun log(priority: Int, message: String, throwable: Throwable)

    fun log(priority: Int, throwable: Throwable?, message: String, vararg args: Any)

    fun isEnabled(priority: Int): Boolean
}
