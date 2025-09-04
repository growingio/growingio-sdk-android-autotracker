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

import android.os.Build
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.SearchEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

internal open class WindowCallbackDelegate(private val delegate: Window.Callback?) : Window.Callback {

    fun getDelegate(): Window.Callback? = delegate

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean = delegate?.dispatchKeyEvent(event) ?: false

    override fun dispatchKeyShortcutEvent(event: KeyEvent?): Boolean = delegate?.dispatchKeyShortcutEvent(event) ?: false

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean = delegate?.dispatchTouchEvent(event) ?: false

    override fun dispatchTrackballEvent(event: MotionEvent?): Boolean = delegate?.dispatchTrackballEvent(event) ?: false

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean = delegate?.dispatchGenericMotionEvent(event) ?: false

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean = delegate?.dispatchPopulateAccessibilityEvent(event) ?: false

    override fun onCreatePanelView(featureId: Int): View? = delegate?.onCreatePanelView(featureId)

    override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean = delegate?.onCreatePanelMenu(featureId, menu) ?: false

    override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean = delegate?.onPreparePanel(featureId, view, menu) ?: false

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean = delegate?.onMenuOpened(featureId, menu) ?: false

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean = delegate?.onMenuItemSelected(featureId, item) ?: false

    override fun onWindowAttributesChanged(attrs: WindowManager.LayoutParams?) {
        delegate?.onWindowAttributesChanged(attrs)
    }

    override fun onContentChanged() {
        delegate?.onContentChanged()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        delegate?.onWindowFocusChanged(hasFocus)
    }

    override fun onAttachedToWindow() {
        delegate?.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        delegate?.onDetachedFromWindow()
    }

    override fun onPanelClosed(featureId: Int, menu: Menu) {
        delegate?.onPanelClosed(featureId, menu)
    }

    override fun onSearchRequested(): Boolean = delegate?.onSearchRequested() ?: false

    override fun onSearchRequested(searchEvent: SearchEvent?): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        delegate?.onSearchRequested(searchEvent) ?: false
    } else {
        false
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback?): ActionMode? = delegate?.onWindowStartingActionMode(callback)

    override fun onWindowStartingActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        delegate?.onWindowStartingActionMode(callback, type)
    } else {
        null
    }

    override fun onActionModeStarted(mode: ActionMode?) {
        delegate?.onActionModeStarted(mode)
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        delegate?.onActionModeFinished(mode)
    }
}
