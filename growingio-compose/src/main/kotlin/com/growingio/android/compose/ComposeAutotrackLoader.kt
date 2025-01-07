/*
 * Copyright (C) 2024 Beijing Yishu Technology Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.growingio.android.compose

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.growingio.android.compose.GrowingComposeKt.path
import com.growingio.android.sdk.TrackerContext
import com.growingio.android.sdk.autotrack.view.ScreenElementHelper
import com.growingio.android.sdk.track.log.Logger
import com.growingio.android.sdk.track.middleware.compose.ComposeData
import com.growingio.android.sdk.track.middleware.compose.ComposeJson
import com.growingio.android.sdk.track.modelloader.DataFetcher
import com.growingio.android.sdk.track.modelloader.ModelLoader
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory
import org.json.JSONArray
import org.json.JSONObject

class ComposeAutotrackLoader(private val context: TrackerContext) : ModelLoader<ComposeData, ComposeJson> {
    override fun buildLoadData(model: ComposeData): ModelLoader.LoadData<ComposeJson> {
        val provider = context.getProvider<ComposeAutotrackProvider>(ComposeAutotrackProvider::class.java)
        return ModelLoader.LoadData(ComposeAutotrackFetcher(model, provider))
    }
}

class Factory(private val context: TrackerContext) : ModelLoaderFactory<ComposeData, ComposeJson> {
    override fun build(): ModelLoader<ComposeData, ComposeJson> {
        return ComposeAutotrackLoader(context)
    }
}

class ComposeAutotrackFetcher(val model: ComposeData, private val provider: ComposeAutotrackProvider?) : DataFetcher<ComposeJson> {

    override fun executeData(): ComposeJson? {
        if (model.decorView == null || provider == null) {
            return null
        }
        val view = model.decorView
        val composeViews = findComposeAndroidView(view)
        if (composeViews.isEmpty()) {
            return null
        }
        val type = model.pageOrViews
        // collect pages
        if (type == 0) {
            val pages = composeViews.fold(arrayListOf<ComposePageNode>()) { list, it ->
                list.addAll(it.collectPages())
                list
            }
            val pageJSONArray = JSONArray()
            pages.forEach {
                val json = JSONObject()
                json.put(ScreenElementHelper.PAGE_PATH, it.alias.path())
                json.put(ScreenElementHelper.PAGE_TITLE, it.alias)
                json.put(ScreenElementHelper.PAGE_LEFT, it.bound.left.toInt())
                json.put(ScreenElementHelper.PAGE_TOP, it.bound.top.toInt())
                json.put(ScreenElementHelper.PAGE_WIDTH, it.bound.width)
                json.put(ScreenElementHelper.PAGE_HEIGHT, it.bound.height)
                json.put(ScreenElementHelper.PAGE_IGNORED, false)
                pageJSONArray.put(json)
            }
            Logger.d("pages", pageJSONArray.toString())
            return ComposeJson(pageJSONArray)
        }

        // collect views
        else if (type == 1) {
            val views = composeViews.fold(arrayListOf<ComposeNode>()) { list, it ->
                list.addAll(it.collectViews())
                list
            }
            val viewJSONArray = JSONArray()
            views.forEach {
                val json = JSONObject()
                json.put(ScreenElementHelper.VIEW_PAGE, it.path())
                json.put(ScreenElementHelper.VIEW_XPATH, it.xpath())
                json.put(ScreenElementHelper.VIEW_XINDEX, it.xIndex())
                json.put(ScreenElementHelper.VIEW_PARENT_XPATH, it.clickableParentXPath())
                json.put(ScreenElementHelper.VIEW_PARENT_XINDEX, it.clickableParentXIndex())
                json.put(ScreenElementHelper.VIEW_LEFT, it.bounds?.left?.toInt())
                json.put(ScreenElementHelper.VIEW_TOP, it.bounds?.top?.toInt())
                json.put(ScreenElementHelper.VIEW_WIDTH, it.bounds?.width?.toInt())
                json.put(ScreenElementHelper.VIEW_HEIGHT, it.bounds?.height?.toInt())
                json.put(ScreenElementHelper.VIEW_NODE_TYPE, it.nodeType())
                json.put(ScreenElementHelper.VIEW_CONTENT, it.calculateText())
                json.put(ScreenElementHelper.VIEW_Z_LEVEL, it.zLevel)
                json.put(ScreenElementHelper.VIEW_INDEX, it.index())
                viewJSONArray.put(json)
            }
            Logger.d("views", viewJSONArray.toString())
            return ComposeJson(viewJSONArray)
        }

        return ComposeJson(null)
    }

    private fun findComposeAndroidView(rootView: View): HashSet<ComposeAndroidView> {
        val composeAndroidViews: HashSet<ComposeAndroidView> = hashSetOf()
        val composeViews = arrayListOf<View>()

        fun recursiveFindComposeView(composeViews: ArrayList<View>, view: View) {
            if (ComposeAndroidView.isComposeView(view)) {
                composeViews.add(view)
            } else if (view is ViewGroup) {
                view.children.forEach {
                    recursiveFindComposeView(composeViews, it)
                }
            }
        }
        recursiveFindComposeView(composeViews, rootView)
        composeViews.forEach {
            composeAndroidViews.add(ComposeAndroidView(it))
        }
        return composeAndroidViews
    }

    override fun getDataClass(): Class<ComposeJson> {
        return ComposeJson::class.java
    }
}
