package com.growingio.sdk.plugin.autotrack.tmp.inject;

import com.growingio.sdk.plugin.autotrack.tmp.Callback;
import com.growingio.sdk.plugin.autotrack.tmp.SuperExample;

public class InjectAgent {
    private static Callback sCallback;

    public static void setsCallback(Callback sCallback) {
        InjectAgent.sCallback = sCallback;
    }

    public static void onExecute(SuperExample example) {
        System.out.println("InjectAgent = " + example);
        if (sCallback != null) {
            sCallback.onCallback(example);
        }
    }
}
