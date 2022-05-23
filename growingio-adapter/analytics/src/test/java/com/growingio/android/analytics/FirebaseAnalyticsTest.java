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

package com.growingio.android.analytics;

import android.app.Application;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;

import androidx.test.core.app.ApplicationProvider;

import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2022/5/23
 */
@Config(manifest = Config.NONE, sdk = 30)
@RunWith(RobolectricTestRunner.class)
public class FirebaseAnalyticsTest {

    Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
        TrackerContext.initSuccess();
        ConfigurationProvider.initWithConfig(new CoreConfiguration("test", "test"), new HashMap<>());
        //FirebaseAnalyticsAdapter.init(application);
    }

    @Test
    public void duelVectorFoilTest() {

        Bundle bundle = new Bundle();
        bundle.putString("s", "String");
        bundle.putStringArray("s[]", new String[]{"s1", "s2"});
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("name");
        arrayList.add("cpacm");
        bundle.putStringArrayList("slist", arrayList);

        bundle.putInt("i", 1);
        bundle.putIntArray("i[]", new int[]{1, 2, 3, 4});
        ArrayList<Integer> arrayList1 = new ArrayList<>();
        arrayList1.add(1);
        arrayList1.add(2);
        bundle.putIntegerArrayList("ilist", arrayList1);

        bundle.putChar("c", 'a');
        bundle.putCharArray("c[]", "cpacm".toCharArray());
        bundle.putCharSequence("cs", "CharSequence");
        bundle.putCharSequenceArray("cs[]", new CharSequence[]{"s1", "s2"});
        ArrayList<CharSequence> arrayList2 = new ArrayList<>();
        arrayList2.add("name");
        arrayList2.add("cpacm");
        bundle.putCharSequenceArrayList("cslist", arrayList2);

        bundle.putByte("b", "cpacm".getBytes()[0]);
        bundle.putByteArray("b[]", "cpacm".getBytes());
        bundle.putFloat("f", 1.0f);
        bundle.putFloatArray("f[]", new float[]{1.0f, 2.0f});
        bundle.putDouble("d", 1d);
        bundle.putDoubleArray("d[]", new double[]{1d, 2d});
        bundle.putShort("st", Short.MIN_VALUE);
        bundle.putShortArray("st[]", new short[]{Short.MIN_VALUE, Short.MAX_VALUE});
        bundle.putBoolean("bool", false);
        bundle.putBooleanArray("bool[]", new boolean[]{false, false, true});

        bundle.putSize("size", new Size(100, 200));
        bundle.putSizeF("sizeF", new SizeF(100f, 200f));

        Bundle deepBundle = bundle.deepCopy();

        bundle.putParcelable("p", deepBundle);
        bundle.putParcelableArray("p[]", new Parcelable[]{deepBundle});
        ArrayList<Parcelable> arrayList3 = new ArrayList<>();
        arrayList3.add(bundle.deepCopy());
        bundle.putParcelableArrayList("plist", arrayList3);
        SparseArray<Parcelable> sa = new SparseArray<>();
        sa.append(100, bundle.deepCopy());
        bundle.putSparseParcelableArray("ps", sa);

        bundle.putSerializable("serial", "cpacm");

        bundle.putBundle("bundle", deepBundle);

        FirebaseAnalyticsAdapter adapter = FirebaseAnalyticsAdapter.get();
        Map<String, String> attr = adapter.parseBundle(bundle);

        Assert.assertEquals(attr.toString(), BUNDLE_RESULT);
    }

    private static final String BUNDLE_RESULT = "{ps_100_plist_0_i=1, ps_100_plist_0_f=1.0, ps_100_plist_0_p_cs[]_1=s2, ps_100_plist_0_d=1.0, ps_100_plist_0_p_cs[]_0=s1, ps_100_plist_0_c=a, ps_100_plist_0_b=99, bundle_ilist_1=2, ps_100_p[]_0_bool=false, ps_100_plist_0_p_bool=false, p[]_0_cslist_1=cpacm, p[]_0_cslist_0=name, bundle_ilist_0=1, ps_100_bool[]_2=true, ps_100_plist_0_p[]_0_c[]_4=m, ps_100_bool[]_1=false, ps_100_d[]_1=2.0, ps_100_plist_0_p[]_0_c[]_0=c, plist_0_slist_0=name, ps_100_plist_0_p[]_0_c[]_1=p, plist_0_slist_1=cpacm, ps_100_plist_0_p[]_0_c[]_2=a, ps_100_d[]_0=1.0, ps_100_plist_0_p[]_0_c[]_3=c, ps_100_bool[]_0=false, ps_100_p[]_0_sizeF=100.0x200.0, plist_0_p[]_0_ilist_0=1, plist_0_p[]_0_ilist_1=2, plist_0_p[]_0_cs=CharSequence, plist_0_bool=false, ps_100_bool=false, ps_100_cs[]_0=s1, size=100x200, ps_100_plist_0_s=String, ps_100_cs[]_1=s2, ps_100_plist_0_p[]_0_bool=false, ps_100_p_cslist_1=cpacm, ps_100_plist_0_p[]_0_ilist_0=1, ps_100_p_cslist_0=name, ps_100_plist_0_p[]_0_ilist_1=2, ps_100_plist_0_s[]_1=s2, ps_100_plist_0_s[]_0=s1, p_b=99, p_d=1.0, ps_100_p_c[]_3=c, p_c=a, ps_100_p_c[]_4=m, p_f=1.0, p_i=1, plist_0_ilist_0=1, plist_0_ilist_1=2, b=99, plist_0_d[]_0=1.0, c=a, d=1.0, p_s=String, f=1.0, i=1, ps_100_p_c[]_0=c, ps_100_p_c[]_1=p, ps_100_p_c[]_2=a, cs[]_1=s2, ps_100_plist_0_p_cslist_0=name, cs[]_0=s1, ps_100_plist_0_p_cslist_1=cpacm, plist_0_d[]_1=2.0, cs=CharSequence, s=String, b[]_4=109, b[]_3=99, b[]_2=97, b[]_1=112, b[]_0=99, plist_0_cs=CharSequence, plist_0_p_cs=CharSequence, ps_100_p[]_0_ilist_1=2, ps_100_plist_0_p[]_0_d[]_1=2.0, ps_100_p[]_0_ilist_0=1, ps_100_plist_0_p[]_0_d[]_0=1.0, plist_0_p[]_0_i=1, plist_0_p[]_0_f=1.0, plist_0_p[]_0_d=1.0, plist_0_p[]_0_b=99, plist_0_p[]_0_c=a, cslist_1=cpacm, p[]_0_i[]_3=4, p[]_0_i[]_2=3, p[]_0_i[]_1=2, p[]_0_i[]_0=1, ps_100_p[]_0_c=a, ps_100_p[]_0_b=99, p_c[]_0=c, ps_100_p[]_0_d=1.0, bundle_d[]_1=2.0, bundle_d[]_0=1.0, plist_0_p[]_0_bool=false, plist_0_p[]_0_s=String, p_c[]_1=p, p_c[]_2=a, p_c[]_3=c, p_c[]_4=m, ps_100_p[]_0_s=String, plist_0_p_b[]_1=112, plist_0_p_b[]_0=99, plist_0_p[]_0_bool[]_0=false, plist_0_p[]_0_bool[]_1=false, plist_0_p[]_0_bool[]_2=true, p_st=-32768, p[]_0_bool=false, ps_100_plist_0_p_cs=CharSequence, ps_100_p[]_0_f=1.0, plist_0_p_b[]_4=109, ps_100_p[]_0_i=1, plist_0_p_b[]_3=99, plist_0_p_b[]_2=97, ps_100_plist_0_d[]_1=2.0, ps_100_s[]_0=s1, plist_0_p[]_0_d[]_0=1.0, ps_100_s[]_1=s2, ps_100_plist_0_d[]_0=1.0, plist_0_p[]_0_d[]_1=2.0, ps_100_plist_0_cs=CharSequence, p[]_0_st[]_0=-32768, p[]_0_st[]_1=32767, plist_0_p_size=100x200, ps_100_p_b[]_4=109, bundle_s[]_0=s1, bundle_s[]_1=s2, plist_0_p[]_0_sizeF=100.0x200.0, ps_100_p[]_0_st=-32768, ps_100_plist_0_p_f[]_1=2.0, p[]_0_ilist_0=1, ps_100_plist_0_p_f[]_0=1.0, p[]_0_ilist_1=2, ps_100_cs=CharSequence, ps_100_plist_0_p[]_0_bool[]_0=false, c[]_3=c, c[]_2=a, ps_100_plist_0_p[]_0_bool[]_1=false, ps_100_plist_0_p[]_0_bool[]_2=true, c[]_4=m, p_d[]_0=1.0, c[]_1=p, p_d[]_1=2.0, c[]_0=c, bundle_st[]_1=32767, plist_0_s[]_0=s1, bundle_st[]_0=-32768, plist_0_s[]_1=s2, ps_100_plist_0_p[]_0_st[]_1=32767, bundle_st=-32768, bundle_slist_0=name, ps_100_plist_0_p[]_0_st[]_0=-32768, bundle_slist_1=cpacm, cslist_0=name, ps_100_p_b[]_2=97, ps_100_p_b[]_3=99, ps_100_p_b[]_0=99, bundle_size=100x200, ps_100_p_b[]_1=112, p_b[]_0=99, p_b[]_1=112, ps_100_plist_0_p_i[]_0=1, plist_0_p[]_0_cs[]_0=s1, ps_100_plist_0_p_i[]_3=4, ps_100_plist_0_p_i[]_1=2, ps_100_plist_0_p_i[]_2=3, ps_100_plist_0_p_size=100x200, p_b[]_4=109, p_b[]_2=97, ps_100_p[]_0_slist_0=name, p_b[]_3=99, ps_100_p[]_0_slist_1=cpacm, p[]_0_f[]_0=1.0, ps_100_p[]_0_bool[]_2=true, ps_100_p[]_0_bool[]_1=false, p[]_0_f[]_1=2.0, ps_100_p[]_0_bool[]_0=false, ps_100_plist_0_c[]_4=m, bundle_c[]_4=m, bundle_s=String, bundle_c[]_3=c, bundle_c[]_2=a, bundle_c[]_1=p, p_i[]_0=1, bundle_c[]_0=c, p_i[]_1=2, p_i[]_2=3, plist_0_p_slist_1=cpacm, plist_0_p_slist_0=name, bundle_sizeF=100.0x200.0, bundle_b=99, ps_100_p_bool=false, bundle_d=1.0, ps_100_i[]_1=2, bundle_c=a, ps_100_i[]_0=1, ps_100_i[]_3=4, ps_100_i[]_2=3, p_i[]_3=4, bundle_i=1, bundle_f=1.0, ps_100_plist_0_c[]_0=c, ps_100_plist_0_c[]_1=p, ps_100_plist_0_c[]_2=a, ps_100_plist_0_c[]_3=c, ps_100_plist_0_p[]_0_i[]_2=3, ps_100_p_slist_1=cpacm, ps_100_plist_0_p[]_0_i[]_3=4, ps_100_p_slist_0=name, p[]_0_st=-32768, ps_100_plist_0_p[]_0_s[]_1=s2, ps_100_plist_0_p[]_0_i=1, ps_100_plist_0_p[]_0_s[]_0=s1, ps_100_plist_0_p[]_0_f=1.0, plist_0_f[]_0=1.0, ps_100_plist_0_p[]_0_s=String, plist_0_f[]_1=2.0, ps_100_plist_0_p[]_0_i[]_0=1, ps_100_plist_0_p[]_0_i[]_1=2, plist_0_p_cslist_0=name, plist_0_p_cslist_1=cpacm, ps_100_plist_0_st[]_0=-32768, ps_100_plist_0_st[]_1=32767, d[]_1=2.0, d[]_0=1.0, ps_100_plist_0_sizeF=100.0x200.0, ps_100_p_st[]_1=32767, ps_100_p_st[]_0=-32768, ps_100_p[]_0_s[]_1=s2, ps_100_p[]_0_s[]_0=s1, ps_100_p_s[]_1=s2, ps_100_plist_0_p[]_0_d=1.0, ps_100_plist_0_p[]_0_b=99, ps_100_p_s[]_0=s1, ps_100_plist_0_p[]_0_c=a, plist_0_p[]_0_cs[]_1=s2, ps_100_plist_0_p[]_0_cs=CharSequence, ps_100_p[]_0_st[]_1=32767, ps_100_p[]_0_st[]_0=-32768, p[]_0_cs[]_0=s1, ps_100_ilist_1=2, plist_0_p[]_0_f[]_0=1.0, ps_100_p[]_0_cs[]_0=s1, ps_100_ilist_0=1, p_st[]_1=32767, ps_100_p[]_0_cs[]_1=s2, plist_0_p[]_0_f[]_1=2.0, p_st[]_0=-32768, plist_0_p_bool=false, p_s[]_0=s1, ps_100_plist_0_p[]_0_b[]_2=97, ps_100_plist_0_p[]_0_b[]_1=112, ps_100_plist_0_p[]_0_b[]_4=109, ps_100_plist_0_p[]_0_b[]_3=99, bundle_b[]_1=112, bundle_b[]_0=99, ps_100_plist_0_b[]_0=99, bundle_b[]_3=99, bundle_b[]_2=97, ps_100_plist_0_b[]_3=99, plist_0_p_cs[]_1=s2, bundle_b[]_4=109, p_s[]_1=s2, ps_100_plist_0_b[]_4=109, plist_0_p_cs[]_0=s1, ps_100_plist_0_b[]_1=112, ps_100_plist_0_b[]_2=97, p[]_0_size=100x200, ps_100_plist_0_p[]_0_b[]_0=99, ps_100_plist_0_ilist_0=1, ps_100_plist_0_ilist_1=2, plist_0_p_i[]_0=1, plist_0_p_i[]_2=3, plist_0_p_i[]_1=2, ps_100_plist_0_p[]_0_size=100x200, ps_100_plist_0_p_ilist_1=2, plist_0_p[]_0_slist_0=name, plist_0_p[]_0_slist_1=cpacm, ps_100_plist_0_p[]_0_cslist_1=cpacm, plist_0_p_i[]_3=4, bundle_bool=false, ps_100_plist_0_p[]_0_cslist_0=name, p_bool=false, ps_100_p_cs=CharSequence, ps_100_plist_0_cslist_0=name, ps_100_p_i[]_0=1, ps_100_plist_0_cslist_1=cpacm, ps_100_p_i[]_2=3, ps_100_p_i[]_1=2, ps_100_p[]_0_b[]_0=99, p[]_0_cs[]_1=s2, ps_100_p_i[]_3=4, ps_100_p[]_0_b[]_3=99, ps_100_p[]_0_b[]_4=109, ps_100_p[]_0_b[]_1=112, ps_100_p[]_0_b[]_2=97, ps_100_plist_0_p_ilist_0=1, p_slist_1=cpacm, plist_0_p[]_0_st=-32768, ps_100_plist_0_p_s=String, p_slist_0=name, ps_100_plist_0_p_i=1, ps_100_plist_0_size=100x200, plist_0_p[]_0_i[]_3=4, plist_0_p[]_0_i[]_2=3, plist_0_p[]_0_i[]_1=2, plist_0_p[]_0_i[]_0=1, ps_100_plist_0_p_b=99, ps_100_plist_0_p_c=a, ps_100_plist_0_p_d=1.0, ps_100_plist_0_p[]_0_sizeF=100.0x200.0, ps_100_plist_0_p_f=1.0, p[]_0_s[]_0=s1, p[]_0_s[]_1=s2, ps_100_p_size=100x200, ps_100_plist_0_p[]_0_cs[]_1=s2, ps_100_plist_0_p[]_0_cs[]_0=s1, plist_0_p[]_0_cslist_1=cpacm, plist_0_p_bool[]_1=false, plist_0_p_bool[]_0=false, plist_0_p[]_0_cslist_0=name, plist_0_p_bool[]_2=true, f[]_0=1.0, f[]_1=2.0, p[]_0_d[]_0=1.0, bundle_i[]_1=2, p[]_0_d[]_1=2.0, plist_0_bool[]_1=false, bundle_i[]_2=3, plist_0_bool[]_2=true, bundle_i[]_3=4, plist_0_bool[]_0=false, ps_100_plist_0_p_st[]_0=-32768, ps_100_plist_0_p_st[]_1=32767, plist_0_p_s[]_0=s1, plist_0_p_s[]_1=s2, ps_100_p_sizeF=100.0x200.0, ps_100_plist_0_p_c[]_0=c, plist_0_p[]_0_b[]_4=109, plist_0_p[]_0_b[]_3=99, ps_100_plist_0_p_c[]_2=a, ps_100_plist_0_p_c[]_1=p, plist_0_p[]_0_b[]_0=99, plist_0_p[]_0_b[]_2=97, plist_0_p[]_0_b[]_1=112, plist_0_p_st[]_0=-32768, plist_0_p_st[]_1=32767, ps_100_p[]_0_c[]_0=c, ps_100_plist_0_p_bool[]_1=false, ps_100_p[]_0_c[]_1=p, ps_100_plist_0_p_bool[]_0=false, st=-32768, ps_100_p[]_0_c[]_2=a, ps_100_p[]_0_c[]_3=c, ps_100_p[]_0_c[]_4=m, ps_100_st[]_1=32767, ps_100_st[]_0=-32768, ps_100_plist_0_p_c[]_4=m, ps_100_plist_0_p_c[]_3=c, plist_0_st=-32768, ps_100_plist_0_p_bool[]_2=true, plist_0_p_st=-32768, p_bool[]_0=false, p_bool[]_1=false, p_bool[]_2=true, p[]_0_bool[]_1=false, ps_100_plist_0_p_s[]_1=s2, p[]_0_bool[]_2=true, bundle_cs=CharSequence, ps_100_plist_0_p_s[]_0=s1, p[]_0_bool[]_0=false, ps_100_f=1.0, p_sizeF=100.0x200.0, ps_100_i=1, ps_100_c=a, ps_100_b=99, ps_100_d=1.0, ps_100_p[]_0_i[]_1=2, ps_100_p[]_0_i[]_0=1, plist_0_p_f[]_1=2.0, plist_0_p_f[]_0=1.0, ps_100_plist_0_p_st=-32768, ps_100_cslist_1=cpacm, ps_100_p[]_0_i[]_3=4, p_ilist_1=2, ps_100_p[]_0_i[]_2=3, p_ilist_0=1, ps_100_cslist_0=name, plist_0_i[]_3=4, plist_0_i[]_2=3, plist_0_i[]_1=2, plist_0_i[]_0=1, plist_0_cs[]_1=s2, ps_100_plist_0_st=-32768, p_cs=CharSequence, plist_0_cs[]_0=s1, ps_100_p_bool[]_2=true, slist_1=cpacm, slist_0=name, ps_100_p_bool[]_0=false, ps_100_s=String, ps_100_p_bool[]_1=false, ps_100_p_f[]_0=1.0, st[]_0=-32768, ps_100_p_f[]_1=2.0, st[]_1=32767, plist_0_p[]_0_c[]_1=p, plist_0_p[]_0_c[]_0=c, bool=false, plist_0_p[]_0_c[]_4=m, plist_0_p[]_0_c[]_3=c, plist_0_p[]_0_c[]_2=a, bundle_i[]_0=1, ps_100_st=-32768, ilist_1=2, ilist_0=1, p_cs[]_0=s1, ps_100_plist_0_i[]_3=4, p_cs[]_1=s2, ps_100_plist_0_i[]_2=3, ps_100_plist_0_i[]_1=2, ps_100_plist_0_i[]_0=1, ps_100_p[]_0_cs=CharSequence, ps_100_plist_0_cs[]_0=s1, ps_100_plist_0_cs[]_1=s2, ps_100_p_s=String, plist_0_size=100x200, sizeF=100.0x200.0, ps_100_plist_0_bool[]_0=false, ps_100_f[]_1=2.0, ps_100_p_i=1, ps_100_plist_0_bool[]_2=true, ps_100_plist_0_bool[]_1=false, bundle_cslist_1=cpacm, ps_100_p[]_0_d[]_1=2.0, bundle_cslist_0=name, ps_100_p[]_0_d[]_0=1.0, ps_100_p_c=a, ps_100_p_d=1.0, ps_100_p_b=99, ps_100_p_f=1.0, ps_100_f[]_0=1.0, p_size=100x200, ps_100_p[]_0_size=100x200, ps_100_plist_0_bool=false, plist_0_sizeF=100.0x200.0, ps_100_slist_0=name, ps_100_slist_1=cpacm, ps_100_plist_0_p_b[]_4=109, ps_100_plist_0_p_b[]_3=99, ps_100_plist_0_p_b[]_2=97, ps_100_plist_0_p_b[]_1=112, ps_100_plist_0_p_b[]_0=99, plist_0_p[]_0_st[]_0=-32768, plist_0_p[]_0_st[]_1=32767, p[]_0_slist_0=name, p[]_0_slist_1=cpacm, ps_100_b[]_3=99, ps_100_b[]_4=109, ps_100_b[]_1=112, plist_0_p_c[]_0=c, ps_100_b[]_2=97, ps_100_b[]_0=99, ps_100_plist_0_slist_1=cpacm, ps_100_plist_0_slist_0=name, plist_0_p_c[]_2=a, plist_0_p_c[]_1=p, plist_0_p_c[]_4=m, plist_0_p_c[]_3=c, ps_100_plist_0_p_slist_0=name, p[]_0_c[]_0=c, plist_0_i=1, p[]_0_c[]_1=p, ps_100_plist_0_p_slist_1=cpacm, ps_100_size=100x200, p[]_0_c[]_4=m, p[]_0_c[]_2=a, p[]_0_c[]_3=c, p[]_0_sizeF=100.0x200.0, plist_0_c=a, plist_0_b=99, plist_0_d=1.0, plist_0_f=1.0, ps_100_p_cs[]_0=s1, ps_100_p_cs[]_1=s2, plist_0_s=String, p[]_0_i=1, p[]_0_d=1.0, p[]_0_f=1.0, p[]_0_c=a, p[]_0_b=99, plist_0_p_sizeF=100.0x200.0, plist_0_st[]_1=32767, p[]_0_cs=CharSequence, plist_0_st[]_0=-32768, bundle_f[]_1=2.0, bundle_f[]_0=1.0, p[]_0_s=String, p_cslist_0=name, p_cslist_1=cpacm, ps_100_c[]_0=c, plist_0_b[]_3=99, ps_100_c[]_1=p, plist_0_b[]_4=109, bundle_cs[]_0=s1, ps_100_c[]_2=a, bundle_cs[]_1=s2, ps_100_c[]_3=c, plist_0_p[]_0_size=100x200, plist_0_p_ilist_0=1, ps_100_plist_0_f[]_1=2.0, plist_0_p_d[]_1=2.0, plist_0_p_ilist_1=2, plist_0_p_d[]_0=1.0, i[]_2=3, i[]_3=4, plist_0_b[]_0=99, i[]_0=1, plist_0_b[]_1=112, ps_100_sizeF=100.0x200.0, i[]_1=2, plist_0_b[]_2=97, ps_100_plist_0_p[]_0_st=-32768, ps_100_plist_0_f[]_0=1.0, plist_0_p[]_0_s[]_0=s1, plist_0_p[]_0_s[]_1=s2, ps_100_p[]_0_cslist_0=name, ps_100_p[]_0_cslist_1=cpacm, ps_100_p_d[]_0=1.0, ps_100_c[]_4=m, ps_100_p_d[]_1=2.0, ps_100_plist_0_p[]_0_slist_0=name, ps_100_plist_0_p[]_0_slist_1=cpacm, plist_0_cslist_0=name, ps_100_plist_0_p_sizeF=100.0x200.0, ps_100_plist_0_p_d[]_1=2.0, ps_100_plist_0_p_d[]_0=1.0, plist_0_cslist_1=cpacm, ps_100_p[]_0_f[]_0=1.0, ps_100_p_ilist_1=2, ps_100_p[]_0_f[]_1=2.0, p[]_0_b[]_3=99, plist_0_p_b=99, p[]_0_b[]_4=109, plist_0_p_f=1.0, p[]_0_b[]_0=99, p[]_0_b[]_1=112, plist_0_p_d=1.0, ps_100_p_st=-32768, p[]_0_b[]_2=97, plist_0_p_c=a, plist_0_p_i=1, plist_0_p_s=String, bundle_bool[]_2=true, bundle_bool[]_0=false, bundle_bool[]_1=false, bool[]_0=false, plist_0_c[]_1=p, plist_0_c[]_0=c, bool[]_2=true, bool[]_1=false, ps_100_p_ilist_0=1, p_f[]_0=1.0, serial=cpacm, p_f[]_1=2.0, s[]_1=s2, s[]_0=s1, ps_100_plist_0_p[]_0_f[]_1=2.0, ps_100_plist_0_p[]_0_f[]_0=1.0, plist_0_c[]_4=m, plist_0_c[]_3=c, plist_0_c[]_2=a}";

}
