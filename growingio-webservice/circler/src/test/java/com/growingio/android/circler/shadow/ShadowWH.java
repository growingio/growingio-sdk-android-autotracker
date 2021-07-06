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

package com.growingio.android.circler.shadow;


import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;
import com.growingio.android.sdk.track.view.DecorView;
import com.growingio.android.sdk.track.view.WindowHelper;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;


@Implements(WindowHelper.class)
public class ShadowWH {

    public static Activity activity;

    @Implementation
    public List<DecorView> getTopActivityViews() {
        View view = activity.getWindow().getDecorView();

        List<DecorView> decorViews = new ArrayList<>();
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];

        Rect area = new Rect(x, y, x + view.getWidth(), y + view.getHeight());
        WindowManager.LayoutParams wlp = new WindowManager.LayoutParams();
        decorViews.add(new DecorView(view, area, wlp));

        return decorViews;
    }
}
