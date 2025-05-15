package com.growingio.android.gmonitor

import com.growingio.android.gmonitor.event.Breadcrumb

/**
 * <p>
 *
 * @author cpacm 2022/9/2
 */
interface ITracker : Cloneable {

    fun trackBreadcrumb(breadcrumb: Breadcrumb)

    public override fun clone(): ITracker
}
