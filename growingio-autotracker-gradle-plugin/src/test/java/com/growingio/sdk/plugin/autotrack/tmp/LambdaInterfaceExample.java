package com.growingio.sdk.plugin.autotrack.tmp;

public class LambdaInterfaceExample extends SuperExample{

    @Override
    public void lambdaExecute() {
        super.lambdaExecute();
    }

    public void setLambda(LambdaInterface lambdaInterface) {
        lambdaInterface.onExecute();
    }

    @Override
    public void onExecute() {
        setLambda(this::lambdaExecute);
        super.onExecute();
    }
}
