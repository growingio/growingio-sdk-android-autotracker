/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
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

package com.growingio.android.sdk.autotrack.view;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.AbsSeekBar;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.growingio.android.sdk.autotrack.shadow.ListMenuItemViewShadow;
import com.growingio.android.sdk.track.utils.ClassExistHelper;

public class ViewUtil {

    private ViewUtil() {
    }

    public static boolean canCircle(View view) {
        return view instanceof WebView
                || isChangeTypeView(view)
                || isClickTypeView(view)
                || ClassExistHelper.isListView(view.getParent())
                || ListMenuItemViewShadow.isListMenuItemView(view);
    }

    public static boolean isChangeTypeView(View view) {
        return view instanceof EditText
                || view instanceof AbsSeekBar
                || isSliderView(view);
    }

    public static boolean isClickTypeView(View view) {
        return (view.isClickable() && view.hasOnClickListeners())
                || view instanceof CompoundButton
                || isMaterialClickView(view);
    }

    private static boolean isSliderView(View view) {
        // Slider
        if (ClassExistHelper.hasClass("com.google.android.material.slider.Slider")) {
            if (view instanceof Slider) {
                Slider slider = (Slider) view;
                return slider.isEnabled();
            }
            if (view instanceof RangeSlider) {
                RangeSlider slider = (RangeSlider) view;
                return slider.isEnabled();
            }
        }
        return false;
    }

    private static boolean isMaterialClickView(View view) {
        // MaterialButtonGroup
        if (ClassExistHelper.hasClass("com.google.android.material.button.MaterialButton")) {
            if (view instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) view;
                return button.isCheckable();
            }
        }

        // TabLayout
        if (ClassExistHelper.hasClass("com.google.android.material.tabs.TabLayout")) {
            if (view instanceof TabLayout.TabView) {
                TabLayout.TabView tabView = (TabLayout.TabView) view;
                return tabView.isEnabled() && tabView.isClickable();
            }
        }

        return false;
    }

    public static String getWidgetContent(View widget) {
        String value = defaultWidgetContentValue(widget);
        if (value != null) return value;
        if (widget instanceof CompoundButton) {
            return compoundButtonContentValue((CompoundButton) widget);
        }
        if (ClassExistHelper.hasClass("com.google.android.material.tabs.TabLayout")) {
            if (widget instanceof TabLayout.TabView) {
                return tabViewContentValue((TabLayout.TabView) widget);
            }
        }
        if (ClassExistHelper.hasClass("com.google.android.material.slider.Slider")) {
            if (widget instanceof Slider) {
                return String.valueOf(((Slider) widget).getValue());
            }
            if (widget instanceof RangeSlider) {
                RangeSlider slider = (RangeSlider) widget;
                if (slider.getValues().size() == 2) {
                    return slider.getValues().get(0) + "-" + slider.getValues().get(1);
                }
            }
        }

        return null;
    }

    private static String tabViewContentValue(TabLayout.TabView tabView) {
        if (tabView.getTab() != null && tabView.getTab().getText() != null) {
            return tabView.getTab().getText().toString();
        } else {
            return null;
        }
    }

    private static String compoundButtonContentValue(CompoundButton button) {
        String content = button.getText().toString();
        String status = String.valueOf(button.isChecked());
        if (content == null || content.isEmpty()) {
            return status;
        } else {
            return content + "[" + status + "]";
        }
    }

    private static String defaultWidgetContentValue(View view) {
        String value = null;
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            if (ViewAttributeUtil.getTrackText(editText) != null) {
                if (!isPasswordInputType(editText.getInputType())) {
                    CharSequence sequence = editText.getText();
                    value = sequence == null ? "" : sequence.toString();
                }
            }
        } else if (view instanceof RatingBar) {
            value = String.valueOf(((RatingBar) view).getRating());
        } else if (view instanceof ProgressBar) {
            value = String.valueOf(((ProgressBar) view).getProgress());
        } else if (view instanceof Spinner) {
            Object item = ((Spinner) view).getSelectedItem();
            if (item instanceof String) {
                value = (String) item;
            } else {
                View selected = ((Spinner) view).getSelectedView();
                if (selected instanceof TextView && ((TextView) selected).getText() != null) {
                    value = ((TextView) selected).getText().toString();
                }
            }
        } else if (view instanceof RadioGroup) {
            RadioGroup group = (RadioGroup) view;
            View selected = group.findViewById(group.getCheckedRadioButtonId());
            if (selected instanceof RadioButton && ((RadioButton) selected).getText() != null) {
                value = ((RadioButton) selected).getText().toString();
            }
        } else if (view instanceof TextView) {
            if (((TextView) view).getText() != null) {
                return ((TextView) view).getText().toString();
            }
        }
        return value;
    }

    private static boolean isPasswordInputType(int inputType) {
        final int variation = inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }
}
