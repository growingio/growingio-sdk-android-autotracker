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

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import com.growingio.android.compose.GrowingComposeKt.path

internal data class ComposePageNode(
    val alias: String,
    val bound: Rect,
    val attributes: Map<String, String>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ComposePageNode) return false
        if (other.alias == alias) return true
        return false
    }

    override fun hashCode(): Int {
        return alias.hashCode()
    }
}

class ComposeNode(val layoutNode: Any) {

    // outer
    var text: String? = null
    var placeOrder: Int = 0
    var measurePolicy: String? = null
    var composableName: String? = null
    var callName: String? = null
    var isEnd: Boolean = false
    var isClickNode: Boolean = false
    var alias: String? = null
    var tag: String? = null
    var bounds: Rect? = null
    var zLevel: Int = 0
    var offset: IntOffset? = null

    var parent: ComposeNode? = null
    var children: ArrayList<ComposeNode> = arrayListOf()
    var attributes: Map<String, String>? = null

    // inner
    private var index: Int = -1
    private var path: String? = null
    private var xpath: String? = null
    private var xIndex: String? = null
    private var clickableParentXPath: String = ""
    private var clickableParentXIndex: String = ""
    private var originXIndexWithoutList: String? = null
    private var nodeType: String = "BUTTON"

    override fun toString(): String {
        return "ComposeNode(text=$text, composableName=$composableName, callName=$callName, isEnd=$isEnd, isClickNode=$isClickNode, alias=$alias, index=$index, tag=$tag, measurePolicy=$measurePolicy)"
    }

    fun appendChildNode(layoutNode: Any): ComposeNode {
        val tempNode = ComposeNode(layoutNode)
        tempNode.parent = this
        tempNode.isEnd = this.isEnd
        tempNode.zLevel = this.zLevel + 1
        children.add(tempNode)
        return tempNode
    }

    fun translateRect(rect: Rect): Rect {
        if (offset != null) {
            val x = offset?.x?.toFloat() ?: 0f
            val y = offset?.y?.toFloat() ?: 0f
            return rect.translate(x, y)
        }
        return rect
    }

    fun calculate() {
        calculatePath()
        calculateXPathAndXIndex()
    }

    private fun isInLazyList(): Boolean {
        return parent?.callName == "LazyRow" ||
            parent?.callName == "LazyColumn" ||
            parent?.callName == "LazyVerticalGrid" ||
            parent?.callName == "LazyHorizontalGrid"
    }

    private fun isList(): Boolean {
        return callName == "LazyRow" ||
            callName == "LazyColumn" ||
            callName == "LazyVerticalGrid" ||
            callName == "LazyHorizontalGrid" ||
            callName == "Column" ||
            callName == "Row"
    }

    private fun calculatePath(): String {
        if (path != null) {
            return path ?: ""
        }
        if (alias != null) {
            path = if (alias!!.startsWith("/")) alias else "/$alias"
            attributes = ComposeAutotrackProvider.findComposePageAttribute(alias!!)
            return path ?: ""
        }
        if (parent == null) {
            path = ""
        } else {
            path = parent?.path
            attributes = parent?.attributes
        }
        return path ?: ""
    }

    private fun calculateXPathAndXIndex() {
        if (xpath != null) {
            return
        }

        index = parent?.index ?: -1

        // 当前组件手动设置了 Modifier.growingTag(tag) 时, 优先取tag值
        val tempXpath = tag.takeIf { !it.isNullOrEmpty() }?.path()
            ?: composableName.takeIf { !it.isNullOrEmpty() && !it.equals("<anonymous>", true) }
                ?.path()
            // 取调用的组件名为路径名称
            ?: callName.takeIf { !it.isNullOrEmpty() }?.path()
            // 在 plugin 中未获得组件调用名时, 以测量策略的前缀作为路径名称
            ?: measurePolicy.takeIf { !it.isNullOrEmpty() }?.replace("MeasurePolicy", "")?.path()
            // 无法取到值，一律设为layout
            ?: "/Layout"

        val tempXIndex = if (tempXpath.isEmpty()) {
            ""
        } else {
            // 过滤 placeOrder 过大值
            if (placeOrder in 1..10000) {
                "/$placeOrder"
            } else {
                "/0"
            }
        }

        // Logger.d("Compose Node","tempXpath: $tempXpath, tempXIndex: $tempXIndex")

        // 当前组件为页面时，则将该节点作为根节点，不计入节点计算
        if (!alias.isNullOrEmpty()) {
            xpath = ""
            xIndex = ""
            originXIndexWithoutList = xIndex
            return
        }

        // 与父组件组装
        if (parent == null) {
            xpath = tempXpath
            xIndex = tempXIndex
            originXIndexWithoutList = xIndex
        } else {
            xpath = (parent?.xpath ?: "") + tempXpath
            if (isInLazyList()) {
                index = placeOrder
                xIndex = (parent?.originXIndexWithoutList ?: "") + "/-"
            } else {
                xIndex = (parent?.xIndex ?: "") + tempXIndex
            }
            originXIndexWithoutList = (parent?.originXIndexWithoutList ?: "") + tempXIndex
        }

        if (parent?.isClickNode == true) {
            clickableParentXPath = parent?.xpath ?: ""
            clickableParentXIndex = parent?.xIndex ?: ""
        } else {
            clickableParentXPath = parent?.clickableParentXPath ?: ""
            clickableParentXIndex = parent?.clickableParentXIndex ?: ""
        }

        if (isList()) {
            nodeType = "LIST"
        } else if (text != null) {
            nodeType = "TEXT"
        }
    }

    fun calculateText(): String? {
        if (text != null) {
            return text
        }
        if (children.isEmpty()) {
            return null
        }
        val sb = StringBuilder()
        for (child in children) {
            if (child.isClickNode) {
                continue
            }
            val childText = child.calculateText()
            if (childText != null) {
                sb.append(childText)
            }
            text = sb.toString()
        }
        return text
    }

    fun index(): Int {
        if (index != -1) {
            return index + 1
        }
        return -1
    }

    fun path() = path ?: ""
    fun xpath() = xpath ?: ""
    fun xIndex() = xIndex ?: ""
    fun clickableParentXPath() = clickableParentXPath
    fun clickableParentXIndex() = clickableParentXIndex
    fun nodeType() = nodeType
    fun isContainInPage(): Boolean {
        if (path.isNullOrEmpty()) {
            return false
        }
        return true
    }
}
