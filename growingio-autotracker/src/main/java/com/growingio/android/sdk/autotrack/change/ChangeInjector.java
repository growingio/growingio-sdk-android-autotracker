package com.growingio.android.sdk.autotrack.change;

import android.view.View;
import android.widget.TextView;

import com.growingio.android.sdk.autotrack.util.ViewHelper;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.sdk.inject.annotation.BeforeSuper;

public class ChangeInjector {
    private static final String TAG = "ChangeInjector";

    private ChangeInjector() {
    }

    @BeforeSuper(clazz = View.OnFocusChangeListener.class, method = "onFocusChange", parameterTypes = {View.class, boolean.class})
    public static void beforeViewOnClick(View.OnClickListener listener, View view, boolean hasFocus) {
        if (view instanceof TextView) {
            LogUtil.d(TAG, "onFocusChanged");
            ViewHelper.changeOn(view);
        }
    }
}