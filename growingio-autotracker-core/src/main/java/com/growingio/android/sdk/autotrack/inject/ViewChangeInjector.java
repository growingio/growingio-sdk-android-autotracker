/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.autotrack.inject;


import android.widget.RatingBar;
import android.widget.SeekBar;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

public class ViewChangeInjector {
    private static final String TAG = "ViewChangeInjector";

    private ViewChangeInjector() {
    }

    public static void seekBarOnStopTrackingTouch(SeekBar.OnSeekBarChangeListener listener, SeekBar seekBar) {
        ViewChangeProvider.seekBarOnProgressChange(seekBar);
    }

    public static void ratingBarOnRatingChange(RatingBar.OnRatingBarChangeListener listener, RatingBar ratingBar, float rating, boolean fromUser) {
        if (fromUser) {
            ViewChangeProvider.ratingBarOnRatingChange(ratingBar, rating);
        }
    }

    public static void sliderOnStopTrackingTouch(Slider.OnSliderTouchListener listener, Slider slider) {
        ViewChangeProvider.sliderOnStopTrackingTouch(slider);
    }

    public static void rangeSliderOnStopTrackingTouch(RangeSlider.OnSliderTouchListener listener, RangeSlider slider) {
        ViewChangeProvider.rangeSliderOnStopTrackingTouch(slider);
    }
}
