package com.growingio.android.gmonitor.utils

/**
 * <p>
 *
 * @author cpacm 2022/9/27
 */
object ExceptionHelper {

    fun getThrowableType(t: Throwable): String = t.javaClass.simpleName

    fun getThrowableMessage(t: Throwable): String = t.message + " at " + t.stackTrace.first().toString()
}
