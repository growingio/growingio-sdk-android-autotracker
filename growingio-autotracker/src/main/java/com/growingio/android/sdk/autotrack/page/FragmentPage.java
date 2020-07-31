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

package com.growingio.android.sdk.autotrack.page;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;

public class FragmentPage extends PageGroup<SuperFragment<?>> {
    public FragmentPage(SuperFragment<?> carrier) {
        super(carrier);
    }

    @Override
    public String getName() {
        if (!TextUtils.isEmpty(getAlias())) {
            return getAlias();
        }

        String tag = getTag();
        if (TextUtils.isEmpty(tag)) {
            tag = "-";
        }
        return "" + getCarrier().getRealFragment().getClass().getSimpleName() +
                "[" + tag + "]";
    }

    @Override
    public View getView() {
        return getCarrier().getView();
    }

    @Override
    String getTag() {
        String tag = getCarrier().getTag();
        if (!TextUtils.isEmpty(tag)) {
            return transformSwitcherTag(tag);
        }
        int id = getCarrier().getId();
        if (id > 0) {
            try {
                return getCarrier().getResources().getResourceEntryName(id);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 如果是ViewPager + Fragment的形式，Fragment的Tag形式是 android:switcher:ViewPagerViewId:Index
     * 例如 android:switcher:2131230945:4
     * 可读性和唯一性比较差
     *
     * @param tag
     * @return
     */
    private String transformSwitcherTag(String tag) {
        String[] e = tag.split(":");
        if (e.length == 4) {
            try {
                e[2] = getCarrier().getResources().getResourceEntryName(Integer.parseInt(e[2]));
            } catch (NumberFormatException ignored) {
            }
            StringBuilder stringBuilder = new StringBuilder(e[0]);
            for (int i = 1; i < e.length; i++) {
                stringBuilder.append(":").append(e[i]);
            }
            return stringBuilder.toString();
        }

        return tag;
    }
}
