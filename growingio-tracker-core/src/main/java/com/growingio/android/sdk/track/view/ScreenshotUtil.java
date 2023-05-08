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

package com.growingio.android.sdk.track.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.PixelCopy;
import android.view.Window;
import android.view.WindowManager;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.webservices.widget.TipView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ScreenshotUtil {
    private ScreenshotUtil() {
    }
    public static Bitmap getScreenshotBitmap() {
        List<DecorView> decorViews = WindowHelper.get().getTopActivityViews();
        for (int i = decorViews.size() - 1; i >= 0; i--) {
            if (decorViews.get(i).getView() instanceof TipView) {
                decorViews.remove(i);
                break;
            }
        }

        DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().getApplicationContext());
        Bitmap bitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
        drawDecorViewsToBitmap(decorViews, bitmap);
        return bitmap;
    }

    private static void drawDecorViewsToBitmap(List<DecorView> decorViews, Bitmap bitmap) {
        if (null != decorViews) {
            for (DecorView decorView : decorViews) {
                drawDecorViewToBitmap(decorView, bitmap);
            }
        }
    }

    private static void drawDecorViewToBitmap(final DecorView decorView, Bitmap bitmap) {
        if ((decorView.getLayoutParams().flags & WindowManager.LayoutParams.FLAG_DIM_BEHIND) == WindowManager.LayoutParams.FLAG_DIM_BEHIND) {
            Canvas dimCanvas = new Canvas(bitmap);
            int alpha = (int) (255 * decorView.getLayoutParams().dimAmount);
            dimCanvas.drawARGB(alpha, 0, 0, 0);
        }
        final Canvas canvas = new Canvas(bitmap);
        canvas.translate(decorView.getRect().left, decorView.getRect().top);
        decorView.getView().draw(canvas);
    }

    public interface ScreenshotCallback {
        void onScreenshot(Bitmap bitmap);
    }

    public static String getScreenshotBase64(Bitmap screenshotBitmap) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        screenshotBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        stream.flush();
        stream.close();
        screenshotBitmap.recycle();
        byte[] bitmapBytes = stream.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
    }

    public static void getScreenshotBitmap(float scale, ScreenshotCallback callback) throws IllegalArgumentException {
        // PixelCopy
        // https://muyangmin.github.io/glide-docs-cn/doc/hardwarebitmaps.html
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Activity activity = ActivityStateProvider.get().getForegroundActivity();
            if (activity == null) getScreenShotBitmapDefault(scale, callback);
            Window window = activity.getWindow();
            DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().getApplicationContext());
            int widthPixels = metrics.widthPixels;
            int heightPixels = metrics.heightPixels;
            int[] location = new int[2];
            Logger.d("ScreenshotUtil", "getScreenshotBitmap: " + widthPixels + " " + heightPixels);
            window.getDecorView().getLocationOnScreen(location);
            Bitmap bitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
            PixelCopy.request(window, new Rect(location[0], location[1], widthPixels, heightPixels),
                    bitmap, copyResult -> {
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback.onScreenshot(scaleBitmap(bitmap, scale));
                        }
                    }, TrackMainThread.trackMain().getMainHandler());
        } else {
            getScreenShotBitmapDefault(scale, callback);
        }

    }

    private static void getScreenShotBitmapDefault(float scale, ScreenshotCallback callback) {
        Bitmap originBitmap = getScreenshotBitmap();
        callback.onScreenshot(scaleBitmap(originBitmap, scale));
    }

    private static Bitmap scaleBitmap(Bitmap bitmap, float scale) {
        if (scale == 1f) return bitmap;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        bitmap.recycle();
        return newBitmap;
    }
}
