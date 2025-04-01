package com.growingio.android.gmonitor.event

import com.growingio.android.gmonitor.Operation
import com.growingio.android.gmonitor.OperationStatus
import com.growingio.android.gmonitor.anr.ApplicationNotRespondingException
import com.growingio.android.gmonitor.utils.ExceptionHelper
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.util.concurrent.ConcurrentHashMap

/**
 * <p>
 *
 * @author cpacm 2022/9/7
 */
data class Breadcrumb(val type: String = TYPE_DEFAULT, val category: String? = null, var message: String? = null) {
    var data = ConcurrentHashMap<String, Any>()

    fun putData(key: String, value: Any): Breadcrumb {
        data.put(key, value)
        return this
    }

    fun putAllData(data: Map<String, Any>): Breadcrumb {
        this.data.putAll(data)
        return this
    }

    override fun toString(): String {
        val sb = StringBuilder("[Breadcrumb]\n")
        sb.append("type:$type").append("\n")
            .append("category:$category").append("\n")
            .append("message:$message").append("\n")
            .append("data:{\n")
        data.forEach { entry ->
            sb.append("    ").append(entry.key).append(":").append(entry.value).append("\n")
        }
        sb.append("}")
        return sb.toString()
    }

    companion object {

        const val TYPE_DEFAULT = "BREADCRUMB"

        const val TYPE_ERROR = "ERROR"
        const val ATTR_ERROR_TYPE = "e_type"
        const val ATTR_ERROR_MESSAGE = "e_message"
        const val ATTR_ERROR_THREAD = "e_thread"
        const val ATTR_ERROR_PID = "e_pid"

        const val CATEGORY_ERROR_EXCEPTION = "EXCEPTION"
        const val CATEGORY_ERROR_ANR = "ANR"
        const val CATEGORY_ERROR_HTTP = "HTTP"

        const val TYPE_PERFORMANCE = "PERFORMANCE"
        const val CATEGORY_PERFORMANCE_APP = "app_start_interval"
        const val CATEGORY_PERFORMANCE_ACTIVITY = "activity_interval"
        const val CATEGORY_PERFORMANCE_FRAGMENT = "fragment_interval"
        const val CATEGORY_PERFORMANCE_OKHTTP3 = "okhttp3"
        const val ATTR_PERFORMANCE_DURATION = "p_page_duration"
        const val ATTR_PERFORMANCE_APP_DURATION = "p_app_duration"
        const val ATTR_PERFORMANCE_APP_COLD = "p_app_cold"
        const val ATTR_PERFORMANCE_PAGE_NAME = "p_name"
        const val ATTR_PERFORMANCE_PAGE_FULLNAME = "p_fullname"
        const val ATTR_PERFORMANCE_LASTPAGE_FULLNAME = "p_last_fullname"

        const val TYPE_HTTP = "NETWORK"

        fun error(category: String = CATEGORY_ERROR_EXCEPTION, e: Throwable): Breadcrumb {
            val breadcrumb = Breadcrumb(TYPE_ERROR, category, e.stackTraceToString())
                .putData(ATTR_ERROR_TYPE, ExceptionHelper.getThrowableType(e))
                .putData(ATTR_ERROR_MESSAGE, ExceptionHelper.getThrowableMessage(e))
                .putData(ATTR_ERROR_PID, android.os.Process.myPid())
            return breadcrumb
        }

        fun anr(e: ApplicationNotRespondingException, stacktrace: String): Breadcrumb {
            val breadcrumb = Breadcrumb(TYPE_ERROR, CATEGORY_ERROR_ANR, stacktrace)
                .putData(ATTR_ERROR_TYPE, ExceptionHelper.getThrowableType(e))
                .putData(ATTR_ERROR_MESSAGE, ExceptionHelper.getThrowableMessage(e))
                .putData(ATTR_ERROR_PID, android.os.Process.myPid())
                .putData(ATTR_ERROR_THREAD, e.thread.name)
            return breadcrumb
        }

        fun pref(op: Operation): Breadcrumb {
            val breadcrumb = Breadcrumb(TYPE_PERFORMANCE, op.name, op.description)
                .putData(ATTR_PERFORMANCE_DURATION, op.getDuration())
                .putAllData(op.data)
            return breadcrumb
        }

        fun operation(op: Operation): Breadcrumb {
            if (op.status == OperationStatus.ERROR) {
                return error(op.name, op.throwable ?: IllegalArgumentException())
            } else {
                return pref(op)
            }
        }

        fun http(url: String, method: String, statusCode: Int? = null): Breadcrumb {
            val breadcrumb = Breadcrumb(TYPE_HTTP)
                .putData("method", method.uppercase())
                .putData("url", url)
            statusCode?.let {
                breadcrumb.putData("statusCode", it)
            }
            return breadcrumb
        }
    }
}
