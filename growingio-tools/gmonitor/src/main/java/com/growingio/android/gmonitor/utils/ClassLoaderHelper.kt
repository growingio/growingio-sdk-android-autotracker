package com.growingio.android.gmonitor.utils

import android.util.Log
import com.growingio.android.gmonitor.GMonitorOption

/**
 * <p>
 *
 * @author cpacm 2022/9/6
 */
object ClassLoaderHelper {

    fun isClassAvailable(clazz: String, logger: ILogger?): Boolean = loadClass(clazz, logger) != null

    fun isClassAvailable(clazz: String, option: GMonitorOption?): Boolean = loadClass(clazz, option?.logger) != null

    private fun loadClass(clazz: String, logger: ILogger?): Any? {
        try {
            return Class.forName(clazz)
        } catch (e1: ClassNotFoundException) {
            logger?.log(Log.DEBUG, "Class not available:$clazz")
        } catch (e2: UnsatisfiedLinkError) {
            logger?.log(Log.ERROR, "Failed to load (UnsatisfiedLinkError) $clazz")
        } catch (e: Throwable) {
            logger?.log(Log.ERROR, "Failed to initialize $clazz")
        }
        return null
    }
}
