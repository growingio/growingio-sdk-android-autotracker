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

package com.growingio.android.sdk.autotrack.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LruCache;
import android.util.SparseArray;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.AbsSeekBar;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.utils.ClassExistHelper;
import com.growingio.android.sdk.track.utils.CustomerInterface;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.utils.WebViewUtil;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.growingio.android.sdk.autotrack.models.ViewNode.ANONYMOUS_CLASS_NAME;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class Util {

    public static final Matcher ID_PATTERN_MATCHER = Pattern.compile("#[\\+\\.a-zA-Z0-9_-]+").matcher("");
    private static final int MAX_CONTENT_LENGTH = 100;
    private static SparseArray<String> sIdMap;
    private static Set<Integer> sBlackListId;
    private static LruCache<Class, String> sClassNameCache = new LruCache<Class, String>(100);

    private Util() {
    }

    public static String getSimpleClassName(Class clazz) {
        String name = sClassNameCache.get(clazz);
        if (TextUtils.isEmpty(name)) {
            name = clazz.getSimpleName();
            if (TextUtils.isEmpty(name)) {
                name = ANONYMOUS_CLASS_NAME;
            }
            synchronized (Util.class) {
                sClassNameCache.put(clazz, name);
            }
            ClassExistHelper.checkCustomRecyclerView(clazz, name);
        }
        return name;
    }

    public static String getViewContent(View view, String bannerText) {
        String value = "";
        String contentTag = ViewAttributeUtil.getContentKey(view);
        if (contentTag != null) {
            value = contentTag;
        } else {
            if (view instanceof EditText) {
                if (ViewAttributeUtil.getTrackText(view) != null) {
                    if (!Util.isPasswordInputType(((EditText) view).getInputType())) {
                        CharSequence sequence = Util.getEditTextText((EditText) view);
                        value = sequence == null ? "" : sequence.toString();
                    }
                }
            } else if (view instanceof RatingBar) {
                value = String.valueOf(((RatingBar) view).getRating());
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
            } else if (view instanceof SeekBar) {
                value = String.valueOf(((SeekBar) view).getProgress());
            } else if (view instanceof RadioGroup) {
                RadioGroup group = (RadioGroup) view;
                View selected = group.findViewById(group.getCheckedRadioButtonId());
                if (selected instanceof RadioButton && ((RadioButton) selected).getText() != null) {
                    value = ((RadioButton) selected).getText().toString();
                }
            } else if (view instanceof TextView) {
                if (((TextView) view).getText() != null) {
                    value = ((TextView) view).getText().toString();
                }
            } else if (view instanceof ImageView) {
                if (!TextUtils.isEmpty(bannerText)) {
                    value = bannerText;
                }
            } else if (view instanceof WebView && !WebViewUtil.isDestroyed((WebView) view) || ClassExistHelper.instanceOfX5WebView(view)) {
                // 后台获取imp时， getUrl必须在主线程
                String url = ViewAttributeUtil.getWebViewUrl(view);
                if (url == null) {
                    if (ThreadUtils.runningOnUiThread()) {
                        if (view instanceof WebView) {
                            url = ((WebView) view).getUrl();
                        } else {
                            url = ((com.tencent.smtt.sdk.WebView) view).getUrl();
                        }
                    } else {
                        postCheckWebViewStatus(view);
                        throw new RuntimeException("WebView getUrl must called on UI Thread");
                    }
                }
                value = url;
            }
            if (TextUtils.isEmpty(value)) {
                if (bannerText != null) {
                    value = bannerText;
                } else if (view.getContentDescription() != null) {
                    value = view.getContentDescription().toString();
                }
            }
        }
        return truncateViewContent(value);
    }

    private static void postCheckWebViewStatus(final View webView) {
        LogUtil.d("GIO.Util", "postCheckWebViewStatus: ", webView);
        ThreadUtils.postOnUiThread(new Runnable() {
            @Override
            public void run() {
                String url = null;
                if (webView instanceof WebView) {
                    url = ((WebView) webView).getUrl();
                } else if (ClassExistHelper.instanceOfX5WebView(webView)) {
                    url = ((com.tencent.smtt.sdk.WebView) webView).getUrl();
                }
                if (url != null) {
                    ViewAttributeUtil.setWebViewUrl(webView, url);
                }
            }
        });
    }

    public static String truncateViewContent(String value) {
        if (value == null) {
            return "";
        }
        if (!TextUtils.isEmpty(value)) {
            if (value.length() > MAX_CONTENT_LENGTH) {
                value = value.substring(0, MAX_CONTENT_LENGTH);
            }
        }
        return encryptContent(value);
    }

    public static boolean isListView(View view) {
        return (view instanceof AdapterView
                || (ClassExistHelper.instanceOfAndroidXRecyclerView(view))
                || (ClassExistHelper.instanceOfAndroidXViewPager(view))
                || (ClassExistHelper.instanceOfSupportRecyclerView(view))
                || (ClassExistHelper.instanceOfSupportViewPager(view)));
    }

    public static String getIdName(View view, boolean fromTagOnly) {
        String idTag = ViewAttributeUtil.getViewId(view);
        if (idTag != null) {
            return idTag;
        }
        if (fromTagOnly) {
            return null;
        }
        if (sIdMap == null) {
            sIdMap = new SparseArray<String>();
        }
        if (sBlackListId == null) {
            sBlackListId = new HashSet<Integer>();
        }
        final int id = view.getId();
        if (id > 0x7f000000 && !sBlackListId.contains(id)) {
            String idName = sIdMap.get(id);
            if (idName != null) {
                return idName;
            }
            synchronized (Util.class) {
                try {
                    idName = view.getResources().getResourceEntryName(id);
                    sIdMap.put(id, idName);
                    return idName;
                } catch (Exception ignored) {
                    sBlackListId.add(id);
                }
            }
        }
        return null;
    }

    public static int calcBannerItemPosition(@NonNull List bannerContent, int position) {
        return position % bannerContent.size();
    }

    public static boolean isIgnoredView(View view) {
        return ViewAttributeUtil.getIgnoreViewKey(view) != null;
    }

    public static boolean isViewClickable(View view) {
        return view.isClickable() || view instanceof RadioGroup || view instanceof Spinner || view instanceof AbsSeekBar
                || (view.getParent() != null && view.getParent() instanceof AdapterView
                && ((AdapterView) view.getParent()).isClickable());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean isPasswordInputType(int inputType) {
        final int variation = inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)
                || variation == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }


    public static CharSequence getEditTextText(TextView textView) {
        try {
            Field mText = TextView.class.getDeclaredField("mText");
            mText.setAccessible(true);
            return (CharSequence) mText.get(textView);
        } catch (Throwable e) {
            LogUtil.d("Util", e);
        }
        return null;
    }


    /**
     * 加密失败,或者数据为空返回原值
     *
     * @return
     */
    public static String encryptContent(String content) {
        final CustomerInterface.Encryption entity = GConfig.getInstance().getEncryption();
        if (entity == null || TextUtils.isEmpty(content)) {
            return content;
        }
        try {
            return entity.encrypt(content);
        } catch (Exception ignore) {
            LogUtil.e("加密失败", "V字段加密算法崩溃，传回content");
            return content;
        }
    }
}

