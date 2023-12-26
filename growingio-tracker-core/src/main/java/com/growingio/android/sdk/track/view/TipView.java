/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.sdk.track.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.growingio.android.sdk.track.R;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.DeviceUtil;

public class TipView extends FrameLayout {
    private final Context mContext;
    private final WindowManager mWindowManager;

    private TextView mContent;
    private TextView mDragTip;

    private boolean mIsNeedShow = false;
    private boolean mIsShowing = false;

    private int mMinMoveDistance;
    private int mViewLastY;
    private float mTouchDownY;

    @SuppressLint("WrongConstant")
    public TipView(Context context) {
        super(context);
        mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        setId(R.id.growing_webservices_tip_view);
        createView();
        setKeepScreenOn(true);
    }

    private void createView() {
        mContent = new TextView(getContext());
        mDragTip = new TextView(getContext());
        mDragTip.setGravity(Gravity.RIGHT);
        mContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mDragTip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        int padding = DeviceUtil.dp2Px(getContext(), 4);
        int paddingVertical = DeviceUtil.dp2Px(getContext(), 6);
        int paddingHorizontal = DeviceUtil.dp2Px(getContext(), 8);
        mContent.setPadding(paddingHorizontal, padding, paddingHorizontal, padding);
        mDragTip.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        mContent.setTextColor(Color.WHITE);
        mDragTip.setTextColor(Color.WHITE);
        mDragTip.setText("如有遮挡请拖动此条");
        addView(mContent, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mDragTip, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setBackgroundResource(R.color.growing_tracker_blue);
        mViewLastY = getStatusBarHeight();
        mMinMoveDistance = ViewConfiguration.get(mContext).getScaledTouchSlop();
    }

    public void setContent(@StringRes int resid) {
        setContent(getContext().getResources().getText(resid));
    }

    public void setContent(CharSequence content) {
        mDragTip.setVisibility(View.VISIBLE);
        mContent.setGravity(Gravity.LEFT);
        setBackgroundResource(R.color.growing_tracker_blue);
        mContent.setText(content);
    }

    public void setErrorMessage(CharSequence message) {
        mDragTip.setVisibility(View.GONE);
        mContent.setGravity(Gravity.CENTER);
        setBackgroundResource(R.color.growing_tracker_orange);
        mContent.setText(message);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownY = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                float rawY = event.getRawY();
                if (Math.abs(rawY - mTouchDownY) < mMinMoveDistance) {
                    break;
                }

                int offsetY = (int) (rawY - mViewLastY);
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
                layoutParams.y += offsetY;
                mViewLastY = layoutParams.y;
                mWindowManager.updateViewLayout(this, layoutParams);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (Math.abs(event.getRawY() - mTouchDownY) < mMinMoveDistance) {
                    performClick();
                }
                break;
            default:
                break;
        }
        return true;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void ready(Activity activity) {
        if (!mIsNeedShow) {
            return;
        }
        show(activity);
    }

    private void addView(IBinder windowToken) {
        if (!mIsShowing) {
            mIsShowing = true;
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
            layoutParams.token = windowToken;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            layoutParams.y = mViewLastY;
            mWindowManager.addView(TipView.this, layoutParams);
        }
    }

    public void remove() {
        if (mIsShowing) {
            try {
                WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                windowManager.removeView(this);
            } catch (Exception e) {
                Logger.e("TipView", e);
            } finally {
                mIsShowing = false;
            }
        }
    }

    public void dismiss() {
        mIsNeedShow = false;
        remove();
    }

    public void show(Activity activity) {
        mIsNeedShow = true;
        if (activity == null) return;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        View decorView = activity.getWindow().getDecorView();
        IBinder windowToken = decorView.getWindowToken();
        if (windowToken == null) {
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    IBinder token = activity.getWindow().getDecorView().getWindowToken();
                    if (token != null) {
                        addView(token);
                        decorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        } else {
            addView(windowToken);
        }
    }
}
