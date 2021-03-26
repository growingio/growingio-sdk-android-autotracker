package com.growingio.sdk.plugin.autotrack.hook;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class HookClassesConfigTest {

    @Test
    public void testAccess() {
        Truth.assertThat(HookClassesConfig.getAroundHookClasses().isEmpty()).isFalse();
        Truth.assertThat(HookClassesConfig.getSuperHookClasses().isEmpty()).isFalse();
    }
}
