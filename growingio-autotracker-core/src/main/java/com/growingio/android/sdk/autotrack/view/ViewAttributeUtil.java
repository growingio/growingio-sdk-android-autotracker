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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.R;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.view.WindowHelper;

import java.util.Arrays;
import java.util.LinkedList;

public class ViewAttributeUtil {

    private static final String TAG = "ViewAttributeUtil";

    private ViewAttributeUtil() {
    }

    public static void setCustomId(View view, String cid) {
        view.setTag(R.id.growing_tracker_view_custom_id, cid);
    }

    public static String getCustomId(View view) {
        Object customId = view.getTag(R.id.growing_tracker_view_custom_id);
        if (customId instanceof String) {
            return (String) customId;
        }

        return null;
    }

    /**
     * 是否采集EditText中text
     */
    public static void setTrackText(View view, Boolean trackText) {
        view.setTag(R.id.growing_tracker_track_text, trackText);
    }

    public static Boolean getTrackText(View view) {
        Object trackText = view.getTag(R.id.growing_tracker_track_text);
        if (trackText instanceof Boolean) {
            return (Boolean) trackText;
        }

        return null;
    }

    public static void setContent(View view, String content) {
        view.setTag(R.id.growing_tracker_view_content, content);
    }

    static String getContent(View view) {
        Object content = view.getTag(R.id.growing_tracker_view_content);
        if (content instanceof String) {
            return (String) content;
        }

        return null;
    }

    public static void setIgnorePolicy(View view, IgnorePolicy policy) {
        view.setTag(R.id.growing_tracker_ignore_policy, policy);
    }

    public static IgnorePolicy getIgnorePolicy(View view) {
        Object ignorePolicy = view.getTag(R.id.growing_tracker_ignore_policy);
        if (ignorePolicy instanceof IgnorePolicy) {
            return (IgnorePolicy) ignorePolicy;
        }

        return null;
    }

    public static void setIgnoreViewClick(View view, boolean isIgnore) {
        view.setTag(R.id.growing_tracker_ignore_view_click, isIgnore);
    }

    public static boolean isIgnoreViewClick(View view) {
        Object ignoreViewClick = view.getTag(R.id.growing_tracker_ignore_view_click);
        if (ignoreViewClick == null) return false;
        if (ignoreViewClick instanceof Boolean) {
            return (Boolean) ignoreViewClick;
        }
        return false;
    }

    static void setMonitoringFocusContent(View view, String text) {
        view.setTag(R.id.growing_tracker_monitoring_focus_content, text);
    }

    static String getMonitoringFocusContent(View view) {
        Object text = view.getTag(R.id.growing_tracker_monitoring_focus_content);
        if (text instanceof String) {
            return (String) text;
        }

        return null;
    }

    public static void setViewPage(View view, Page<?> page) {
        view.setTag(R.id.growing_tracker_view_page, page);
    }

    public static Page<?> getViewPage(View view) {
        Object page = view.getTag(R.id.growing_tracker_view_page);
        if (page instanceof Page) {
            return (Page<?>) page;
        }
        return null;
    }

    public static boolean isIgnoredView(View view) {
        IgnorePolicy ignorePolicy = ViewAttributeUtil.getIgnorePolicy(view);
        if (ignorePolicy != null) {
            return ignorePolicy == IgnorePolicy.IGNORE_SELF || ignorePolicy == IgnorePolicy.IGNORE_ALL;
        }
        return isIgnoredByParent(view);
    }

    private static boolean isIgnoredByParent(View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            IgnorePolicy ignorePolicy = ViewAttributeUtil.getIgnorePolicy((View) parent);
            if ((ignorePolicy == IgnorePolicy.IGNORE_ALL || ignorePolicy == IgnorePolicy.IGNORE_CHILD)) {
                return true;
            }
            return isIgnoredByParent((View) parent);
        }
        return false;
    }

    private static final int PACKAGE_ID_START = 0x7f000000;

    @Nullable
    static String getViewPackageId(View view) {
        return getPackageId(view.getContext(), view.getId());
    }


    static String getPackageId(Context context, int id) {
        try {
            if (id <= PACKAGE_ID_START) {
                return null;
            }
            return context.getResources().getResourceEntryName(id);
        } catch (Resources.NotFoundException e) {
            Logger.e(TAG, e.getMessage());
        } catch (NullPointerException ignored) {
        }
        return null;
    }

    private static final int MAX_CONTENT_LENGTH = 100;

    /**
     * it's a loop content finder.
     */
    static String findViewContent(View view) {
        LinkedList<View> queue = new LinkedList<>();
        queue.add(view);
        while (!queue.isEmpty()) {
            View childView = queue.poll();
            String content = findViewContentBfs(childView, queue);
            if (!TextUtils.isEmpty(content)) return content;
        }
        return null;
    }

    private static String findViewContentBfs(View view, LinkedList<View> queue) {
        String findContent = getViewContent(view);
        if (TextUtils.isEmpty(findContent) && view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            View[] children = new View[vg.getChildCount()];
            for (int i = 0; i < vg.getChildCount(); i++) {
                children[i] = vg.getChildAt(i);
            }
            Arrays.sort(children, (o1, o2) -> {
                int scoreA = 0;
                scoreA += isViewInvisible(o1) ? 0 : 100;
                scoreA += o1 instanceof TextView ? 10 : 0;

                int scoreB = 0;
                scoreB += isViewInvisible(o2) ? 0 : 100;
                scoreB += o2 instanceof TextView ? 10 : 0;

                if (scoreA == scoreB) {
                    return o2.getHeight() * o2.getWidth() - o1.getHeight() * o1.getWidth();
                }
                return scoreB - scoreA;
            });

            for (View child : children) {
                findContent = getViewContent(child);
                if (!TextUtils.isEmpty(findContent)) {
                    return findContent;
                }
                queue.add(child);
            }
        }
        return findContent;
    }

    static String getViewContent(View view) {
        String value = "";
        String contentTag = getContent(view);
        if (contentTag != null) {
            value = contentTag;
        } else {
            value = ViewUtil.getWidgetContent(view);
            if (TextUtils.isEmpty(value)) {
                if (view.getContentDescription() != null) {
                    value = view.getContentDescription().toString();
                }
            }
        }
        return truncateViewContent(value);
    }

    private static String truncateViewContent(String value) {
        if (value == null) {
            return "";
        }
        if (!TextUtils.isEmpty(value)) {
            if (value.length() > MAX_CONTENT_LENGTH) {
                value = value.substring(0, MAX_CONTENT_LENGTH);
            }
        }
        return value;
    }

    static boolean isViewInvisible(View view) {
        return view.getVisibility() == View.GONE || view.getWidth() <= 0 || view.getHeight() <= 0;
    }

    static boolean isViewSelfVisible(View mView) {
        if (mView == null || mView.getWindowVisibility() == View.GONE) {
            return false;
        }

        // home键back后, DecorView的visibility是 INVISIBLE, 即onResume时Window并不可见, 对GIO而言此时是可见的
        if (WindowHelper.get().isDecorView(mView)) {
            return true;
        }

        if (!(mView.getWidth() > 0
                && mView.getHeight() > 0
                && mView.getAlpha() > 0
                && mView.getLocalVisibleRect(new Rect()))) {
            return false;
        }

        //动画导致用户可见但是仍然 invisible,
        if (mView.getVisibility() != View.VISIBLE
                && mView.getAnimation() != null
                && mView.getAnimation().getFillAfter()) {
            return true;
        } else {
            return mView.getVisibility() == View.VISIBLE;
        }

    }

    public static boolean viewVisibilityInParents(View view) {
        if (view == null) {
            return false;
        }

        if (!isViewSelfVisible(view)) {
            return false;
        }

        ViewParent viewParent = view.getParent();
        while (viewParent instanceof View) {
            if (isViewSelfVisible((View) viewParent)) {
                viewParent = viewParent.getParent();
                if (viewParent == null) {
                    Logger.d(TAG, "Hit detached view: ", viewParent);
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    static boolean hasEditTextChanged(View view) {
        if (view instanceof EditText) {
            String tag = getMonitoringFocusContent(view);
            String lastText = tag == null ? "" : tag;
            String nowText = ((EditText) view).getText().toString();
            if ((TextUtils.isEmpty(nowText) && TextUtils.isEmpty(lastText)) || lastText.equals(nowText)) {
                return false;
            }
            setMonitoringFocusContent(view, nowText);
            return true;
        }
        return true;
    }

}
