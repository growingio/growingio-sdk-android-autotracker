package com.growingio.sdk.plugin.autotrack.tmp;

public class SuperExample {
    private boolean mIsExecuted = false;

    public boolean isExecuted() {
        return mIsExecuted;
    }

    public void onExecute() {
        mIsExecuted = true;
    }
}
