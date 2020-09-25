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

package com.growingio.android.sdk.autotrack.webservices;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.autotrack.view.WindowHelper;
import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.autotrack.view.DecorView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ScreenshotUtil {
    private ScreenshotUtil() {
    }

    public static String getScreenshotBase64(float scale) throws IOException {
        Bitmap screenshotBitmap = getScreenshotBitmap(scale);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        screenshotBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        stream.flush();
        stream.close();
        screenshotBitmap.recycle();
        byte[] bitmapBytes = stream.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
    }

    public static Bitmap getScreenshotBitmap(float scale) {
        Bitmap originBitmap = getScreenshotBitmap();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newBitmap = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(), originBitmap.getHeight(), matrix, false);
        if (newBitmap.equals(originBitmap)) {
            return originBitmap;
        }
        originBitmap.recycle();
        return newBitmap;
    }

    public static Bitmap getScreenshotBitmap() {
        DecorView[] decorViews = WindowHelper.get().getTopActivityViews();
        DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(ContextProvider.getApplicationContext());
        Bitmap bitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
        drawDecorViewsToBitmap(decorViews, bitmap);
        return bitmap;
    }

    private static void drawDecorViewsToBitmap(DecorView[] decorViews, Bitmap bitmap) {
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
}
