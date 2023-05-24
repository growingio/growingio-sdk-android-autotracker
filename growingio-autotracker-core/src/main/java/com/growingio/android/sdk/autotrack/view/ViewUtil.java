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
import android.webkit.WebView;
import android.widget.AbsSeekBar;
import android.widget.CompoundButton;
import android.widget.EditText;

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
                || view instanceof CompoundButton
                || view instanceof AbsSeekBar
                || view instanceof EditText
                || (view.isClickable() && view.hasOnClickListeners())
                || ClassExistHelper.isListView(view.getParent())
                || ListMenuItemViewShadow.isListMenuItemView(view)
                || isMaterialCircleView(view);
    }

    public static boolean isMaterialCircleView(View view) {
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
}
