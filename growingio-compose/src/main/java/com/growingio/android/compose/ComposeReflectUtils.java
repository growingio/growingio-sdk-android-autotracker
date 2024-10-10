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
package com.growingio.android.compose;

import androidx.annotation.Nullable;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.AnnotatedString;

import com.growingio.android.sdk.track.log.Logger;

import java.lang.reflect.Field;

public class ComposeReflectUtils {

    private final static String TAG = "ComposeReflectUtils";
    private final static String CLICKABLE_ELEMENT_CLASSNAME = "androidx.compose.foundation.ClickableElement";
    private final static String COMBINED_CLICKABLE_ELEMENT_CLASSNAME = "androidx.compose.foundation.CombinedClickableElement";

    // select/toggle
    private final static String SELECTABLE_ELEMENT_CLASSNAME = "androidx.compose.foundation.selection.SelectableElement";
    private final static String TOGGLEABLE_ELEMENT_CLASSNAME = "androidx.compose.foundation.selection.ToggleableElement";
    private final static String TRI_TOGGLEABLE_ELEMENT_CLASSNAME = "androidx.compose.foundation.selection.TriStateToggleableElement";

    // text
    private final static String TEXT_STRING_SIMPLE_ELEMENT_CLASSNAME = "androidx.compose.foundation.text.modifiers.TextStringSimpleElement";
    private final static String TEXT_ANNOTATED_STRING_ELEMENT_CLASSNAME = "androidx.compose.foundation.text.modifiers.TextAnnotatedStringElement";
    private final static String SELECTABLE_TEXT_ANNOTATED_STRING_ELEMENT_CLASSNAME = "androidx.compose.foundation.text.modifiers.SelectableTextAnnotatedStringElement";


    private static Field layoutDelegateField = null;

    private static Field clickableElementField = null;
    private static Field combinedClickableElementField = null;
    private static Field selectableElementField = null;
    private static Field toggleableElementField = null;
    private static Field triToggleableElementField = null;

    private static Field textStringSimpleElementField = null;
    private static Field textAnnotatedStringElementField = null;
    private static Field selectableTextAnnotatedStringElementField = null;

    public static Field getLayoutDelegateField() {
        if (layoutDelegateField == null) {
            try {
                final Class<?> clazz = Class.forName("androidx.compose.ui.node.LayoutNode");
                layoutDelegateField = clazz.getDeclaredField("layoutDelegate");
                layoutDelegateField.setAccessible(true);
            } catch (Exception ignored) {
                Logger.e(TAG, "Failed to get layoutDelegate field from LayoutNode.");
            }
        }
        return layoutDelegateField;
    }

    public static boolean isClickableElement(Modifier modifier) {
        final @Nullable String type = modifier.getClass().getCanonicalName();
        if (CLICKABLE_ELEMENT_CLASSNAME.equals(type)) {
            if (clickableElementField == null) {
                try {
                    clickableElementField = modifier.getClass().getDeclaredField("enabled");
                    clickableElementField.setAccessible(true);
                } catch (Exception e) {
                    Logger.e(TAG, "Failed to get clickable field from ClickableElement.");
                }
            }
            try {
                return (Boolean) clickableElementField.get(modifier);
            } catch (Exception e) {
                Logger.e(TAG, "Failed to get clickable field value from ClickableElement.");
            }
            return false;
        } else if (COMBINED_CLICKABLE_ELEMENT_CLASSNAME.equals(type)) {
            if (combinedClickableElementField == null) {
                try {
                    combinedClickableElementField = modifier.getClass().getDeclaredField("enabled");
                    combinedClickableElementField.setAccessible(true);
                } catch (Exception e) {
                    Logger.e(TAG, "Failed to get clickable field from ClickableElement.");
                }
            }
            try {
                return (Boolean) combinedClickableElementField.get(modifier);
            } catch (Exception e) {
                Logger.e(TAG, "Failed to get clickable field value from ClickableElement.");
            }
            return false;
        } else if (SELECTABLE_ELEMENT_CLASSNAME.equals(type)){
            if (selectableElementField == null) {
                try {
                    selectableElementField = modifier.getClass().getDeclaredField("enabled");
                    selectableElementField.setAccessible(true);
                } catch (Exception e) {
                    Logger.e(TAG, "Failed to get clickable field from SelectableElement.");
                }
            }
            try {
                return (Boolean) selectableElementField.get(modifier);
            } catch (Exception e) {
                Logger.e(TAG, "Failed to get clickable field value from SelectableElement.");
            }
            return false;
        } else if (TOGGLEABLE_ELEMENT_CLASSNAME.equals(type)){
            if (toggleableElementField == null) {
                try {
                    toggleableElementField = modifier.getClass().getDeclaredField("enabled");
                    toggleableElementField.setAccessible(true);
                } catch (Exception e) {
                    Logger.e(TAG, "Failed to get clickable field from ToggleableElement.");
                }
            }
            try {
                return (Boolean) toggleableElementField.get(modifier);
            } catch (Exception e) {
                Logger.e(TAG, "Failed to get clickable field value from ToggleableElement.");
            }
            return false;
        } else if (TRI_TOGGLEABLE_ELEMENT_CLASSNAME.equals(type)){
            if (triToggleableElementField == null) {
                try {
                    triToggleableElementField = modifier.getClass().getDeclaredField("enabled");
                    triToggleableElementField.setAccessible(true);
                } catch (Exception e) {
                    Logger.e(TAG, "Failed to get clickable field from TriStateToggleableElement.");
                }
            }
            try {
                return (Boolean) triToggleableElementField.get(modifier);
            } catch (Exception e) {
                Logger.e(TAG, "Failed to get clickable field value from TriStateToggleableElement.");
            }
        }
        return false;
    }

    public static String getTextFromTextElement(Modifier modifier) {
        final @Nullable String type = modifier.getClass().getCanonicalName();
        if (TEXT_STRING_SIMPLE_ELEMENT_CLASSNAME.equals(type)) {
            if (textStringSimpleElementField == null) {
                try {
                    textStringSimpleElementField = modifier.getClass().getDeclaredField("text");
                    textStringSimpleElementField.setAccessible(true);
                } catch (Exception e) {
                    Logger.e(TAG, "Failed to get text field from TextStringSimpleElement.");
                }
            }
            try {
                return (String) textStringSimpleElementField.get(modifier);
            } catch (Exception e) {
                Logger.e(TAG, "Failed to get text field value from TextStringSimpleElement.");
            }
            return null;
        } else if (TEXT_ANNOTATED_STRING_ELEMENT_CLASSNAME.equals(type)) {
            if (textAnnotatedStringElementField == null) {
                try {
                    textAnnotatedStringElementField = modifier.getClass().getDeclaredField("text");
                    textAnnotatedStringElementField.setAccessible(true);
                } catch (Exception e) {
                    Logger.e(TAG, "Failed to get text field from TextAnnotatedStringElement.");
                }
            }
            try {
                return ((AnnotatedString) textAnnotatedStringElementField.get(modifier)).getText();
            } catch (Exception e) {
                Logger.e(TAG, "Failed to get text field value from TextAnnotatedStringElement.");
            }
            return null;
        } else if (SELECTABLE_TEXT_ANNOTATED_STRING_ELEMENT_CLASSNAME.equals(type)) {
            if (selectableTextAnnotatedStringElementField == null) {
                try {
                    selectableTextAnnotatedStringElementField = modifier.getClass().getDeclaredField("text");
                    selectableTextAnnotatedStringElementField.setAccessible(true);
                } catch (Exception e) {
                    Logger.e(TAG, "Failed to get text field from SelectableTextAnnotatedStringElement.");
                }
            }
            try {
                return ((AnnotatedString) selectableTextAnnotatedStringElementField.get(modifier)).getText();
            } catch (Exception e) {
                Logger.e(TAG, "Failed to get text field value from SelectableTextAnnotatedStringElement.");
            }
        }
        return null;
    }
}
