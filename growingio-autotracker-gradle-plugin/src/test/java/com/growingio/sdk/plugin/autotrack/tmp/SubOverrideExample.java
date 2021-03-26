package com.growingio.sdk.plugin.autotrack.tmp;

public class SubOverrideExample extends SuperExample {
    @Override
    public void onExecute() {
        originExecute();
        originExecuteWithArg("arg");
        super.onExecute();
    }
}
