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

package com.growingio.android.sdk.track.utils;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.providers.RobolectricActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.util.Map;

@RunWith(RobolectricTestRunner.class)
public class UtilsTest {

    @Test
    public void activityTest() {
        ActivityController<RobolectricActivity> activityController = Robolectric.buildActivity(RobolectricActivity.class);
        RobolectricActivity activity = activityController.create().get();
        Truth.assertThat(activity).isEqualTo(ActivityUtil.findActivity(activity.textView));
        activityController.destroy();
        Truth.assertThat(ActivityUtil.isDestroy(activity)).isTrue();
        Truth.assertThat(ActivityUtil.findActivity(new ContextWrapper(activity))).isEqualTo(activity);
    }

    @Test
    public void classExistTest() {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().get();
        //Truth.assertThat(ClassExistHelper.hasMSA()).isFalse();
        Truth.assertThat(ClassExistHelper.instanceOfAndroidXAlertDialog(new Dialog(activity))).isFalse();
        Truth.assertThat(ClassExistHelper.instanceOfAndroidXFragment(new Fragment())).isTrue();
        Truth.assertThat(ClassExistHelper.instanceOfAndroidXFragmentActivity(activity)).isFalse();
        //Truth.assertThat(ClassExistHelper.instanceOfAndroidXListMenuItemView(new ListMenuItemView(activity, null,0))).isTrue();
        Truth.assertThat(ClassExistHelper.instanceOfAndroidXRecyclerView(new RecyclerView(activity, null))).isTrue();
        Truth.assertThat(ClassExistHelper.instanceofAndroidXSwipeRefreshLayout(new SwipeRefreshLayout(activity, null))).isTrue();
        Truth.assertThat(ClassExistHelper.instanceOfAndroidXViewPager(new ViewPager(activity, null))).isTrue();
        Truth.assertThat(ClassExistHelper.instanceOfRecyclerView(new RecyclerView(activity, null))).isTrue();

        Truth.assertThat(ClassExistHelper.instanceOfSupportAlertDialog(new Dialog(activity))).isFalse();
        Truth.assertThat(ClassExistHelper.instanceOfSupportFragment(new android.app.Fragment())).isFalse();
        Truth.assertThat(ClassExistHelper.instanceOfSupportFragmentActivity(activity)).isFalse();
        //Truth.assertThat(ClassExistHelper.instanceOfSupportListMenuItemView(new ListMenuItemView(activity, null))).isFalse();
        Truth.assertThat(ClassExistHelper.instanceOfSupportRecyclerView(new RecyclerView(activity, null))).isFalse();
        Truth.assertThat(ClassExistHelper.instanceOfSupportSwipeRefreshLayout(new SwipeRefreshLayout(activity, null))).isFalse();
        Truth.assertThat(ClassExistHelper.instanceOfSupportViewPager(new ViewPager(activity, null))).isFalse();

        Truth.assertThat(ClassExistHelper.instanceOfUcWebView(new WebView(activity, null))).isFalse();
        Truth.assertThat(ClassExistHelper.instanceOfX5WebView(new WebView(activity, null))).isFalse();
        Truth.assertThat(ClassExistHelper.isListView(new ListView(activity, null))).isTrue();
        Truth.assertThat(ClassExistHelper.isWebView(new WebView(activity, null))).isTrue();
    }

    @Test
    public void deviceTest() {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().get();
        DisplayMetrics display = DeviceUtil.getDisplayMetrics(activity);
        Truth.assertThat(display.density).isEqualTo(1.0f);
        Truth.assertThat(display.widthPixels).isEqualTo(320);
        Truth.assertThat(display.heightPixels).isEqualTo(470);
        Truth.assertThat(display.xdpi).isEqualTo(160.0f);
        Truth.assertThat(display.ydpi).isEqualTo(160.0f);
        Truth.assertThat(display.scaledDensity).isEqualTo(1.0f);

        Truth.assertThat(DeviceUtil.dp2Px(activity, 10)).isEqualTo(10);
        Truth.assertThat(DeviceUtil.sp2Px(activity, 10)).isEqualTo(10);
        Truth.assertThat(DeviceUtil.isPhone(activity)).isTrue();
    }

    @Test
    public void jsonTest() throws JSONException {
        String json = "{\n" +
                "\t\t\"id\": 52921,\n" +
                "\t\t\"title\": \"\\u8f6c\\u77ac\\u5373\\u901d\\u7684\\u6e4a\",\n" +
                "\t\t\"authors\": \"\\u5317\\u5c4b\\u3051\\u3051\",\n" +
                "\t\t\"types\": \"\\u7231\\u60c5\\/\\u6821\\u56ed\\/\\u5947\\u5e7b\",\n" +
                "\t\t\"status\": \"\\u8fde\\u8f7d\\u4e2d\"" +
                "\t}";
        Map<String, String> map = JsonUtil.copyToMap(new JSONObject(json));
        Truth.assertThat(map.get("types")).isEqualTo("爱情/校园/奇幻");
        Truth.assertThat(map.get("id")).isEqualTo("52921");
        Truth.assertThat(map.get("title")).isEqualTo("转瞬即逝的湊");
        Truth.assertThat(map.get("authors")).isEqualTo("北屋けけ");
        Truth.assertThat(map.get("status")).isEqualTo("连载中");
    }

    Application application = ApplicationProvider.getApplicationContext();

    @Test
    public void networkTest() {
        NetworkUtil.NetworkState state = NetworkUtil.getActiveNetworkState(application);
        Truth.assertThat(state.isWifi()).isFalse();
        Truth.assertThat(state.isMobileData()).isTrue();
        Truth.assertThat(state.isConnected()).isTrue();
        Truth.assertThat(state.getNetworkName()).isEqualTo("2G");
        Truth.assertThat(NetworkUtil.getNetworkName(ConnectivityManager.TYPE_MOBILE, TelephonyManager.NETWORK_TYPE_CDMA, "Test Network")).isEqualTo("3G");
        Truth.assertThat(NetworkUtil.getNetworkName(ConnectivityManager.TYPE_MOBILE, TelephonyManager.NETWORK_TYPE_LTE, "Test Network")).isEqualTo("4G");
        Truth.assertThat(NetworkUtil.getNetworkName(ConnectivityManager.TYPE_WIFI, TelephonyManager.NETWORK_TYPE_LTE, "Test Network")).isEqualTo("WIFI");
    }


    @Test
    public void objectTest() {
        Truth.assertThat(ObjectUtils.equals("asdad", "asdad")).isTrue();
        Truth.assertThat(ObjectUtils.toString(new StringBuilder("cpacm"))).isEqualTo("cpacm");
        Truth.assertThat(ObjectUtils.hashCode("aa")).isEqualTo(3104);
    }

    @Test
    public void permissionTest() {
        TrackerContext.init(application);
        Truth.assertThat(PermissionUtil.hasAccessNetworkStatePermission()).isTrue();
        Truth.assertThat(PermissionUtil.hasInternetPermission()).isTrue();
        Truth.assertThat(PermissionUtil.hasWriteExternalPermission()).isFalse();
    }

    @Test
    public void systemTest() {
        Truth.assertThat(SystemUtil.getProcessName()).isNull();
        Truth.assertThat(SystemUtil.isMainProcess(application)).isFalse();
        //SystemUtil.killAppProcess(application);

        SysTrace.beginSection("test");
        SysTrace.endSection();
    }

    @Test
    public void threadTest() {
        ThreadUtils.setUiThread(Looper.myLooper());
        Truth.assertThat(ThreadUtils.runningOnUiThread()).isTrue();
        ThreadUtils.setWillOverrideUiThread();
        ThreadUtils.runOnUiThread(this::objectTest);
        ThreadUtils.postOnUiThreadDelayed(this::objectTest, 1000L);
    }

}
