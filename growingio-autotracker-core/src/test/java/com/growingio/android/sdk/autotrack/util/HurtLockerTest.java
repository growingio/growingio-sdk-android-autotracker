package com.growingio.android.sdk.autotrack.util;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.events.VisitEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

/**
 * 通过利用系统类反射调用， 从而绕过检测
 */
@RunWith(PowerMockRunner.class)
public class HurtLockerTest {

    @Test
    public void testHurtLock() throws Exception {
        int maxSize = HurtLocker.getInternalState(new ArrayList<String>(), "MAX_ARRAY_SIZE");
        Truth.assertThat(maxSize == (Integer.MAX_VALUE - 8)).isTrue();
        String superValue = HurtLocker.getInternalState(new SubClass(), "superValue");
        Truth.assertThat(superValue).isEqualTo("superValue");
    }
}

class SuperClass {
    private String superValue = "superValue";
}

class SubClass extends SuperClass {
}
