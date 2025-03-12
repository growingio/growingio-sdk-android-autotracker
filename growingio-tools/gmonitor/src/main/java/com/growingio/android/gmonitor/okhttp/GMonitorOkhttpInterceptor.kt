package com.growingio.android.gmonitor.okhttp

import com.growingio.android.gmonitor.ITracker
import com.growingio.android.gmonitor.Operation
import com.growingio.android.gmonitor.OperationStatus
import com.growingio.android.gmonitor.TrackerProvider
import com.growingio.android.gmonitor.event.Breadcrumb
import com.growingio.android.gmonitor.event.Breadcrumb.Companion.CATEGORY_PERFORMANCE_OKHTTP3
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * <p>
 *
 * @author cpacm 2022/9/7
 */
class GMonitorOkhttpInterceptor(private val tracker: ITracker = TrackerProvider.instance) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // read transaction from the bound scope
        val op = Operation(CATEGORY_PERFORMANCE_OKHTTP3)
        var request = chain.request()
        val url = request.url().toString()
        val method = request.method()
        op.putData("url", url)
        op.putData("method", method.uppercase())

        var response: Response? = null

        var code: Int? = null
        try {
            val requestBuilder = request.newBuilder()
            request = requestBuilder.build()
            response = chain.proceed(request)
            code = response.code()
            return response
        } catch (e: IOException) {
            op.setStatus(OperationStatus.ERROR, e)
            throw e
        } finally {
            op.putData("status_code", code ?: 200)
            op.finish()
            val breadcrumb = Breadcrumb.operation(op)
            request.body()?.contentLength().ifHasValidLength {
                breadcrumb.putData("request_body_size", it)
            }
            response?.let {
                it.body()?.contentLength().ifHasValidLength { responseBodySize ->
                    breadcrumb.putData("response_body_size", responseBodySize)
                }
            }
            tracker.trackBreadcrumb(breadcrumb)
        }
    }

    private fun Long?.ifHasValidLength(fn: (Long) -> Unit) {
        if (this != null && this != -1L) {
            fn.invoke(this)
        }
    }
}
