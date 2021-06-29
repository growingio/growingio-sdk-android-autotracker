package com.growingio.android.sdk.track.events;

public enum FilterType {

    VISIT,
    CUSTOM,
    VISITOR_ATTRIBUTES,
    LOGIN_USER_ATTRIBUTES,
    CONVERSION_VARIABLES,
    APP_CLOSED,
    PAGE,
    PAGE_ATTRIBUTES,
    VIEW_CLICK,
    VIEW_CHANGE,
    FORM_SUBMIT,
    REENGAGE;

    private final int mask;

    FilterType() {
        mask = (1 << ordinal());
    }

    public final int getMask() {
        return mask;
    }

    public static int of(FilterType... types) {

        if (types == null) {
            return 0;
        }

        int value = 0;
        for (FilterType type : types) {
            value |= type.mask;
        }
        return value;
    }

    // 拼接日志
    public static String printLog(FilterType... types) {

        StringBuilder logStr = new StringBuilder();

        for (int i = 0; i < types.length - 1; i++) {
            logStr.append(types[i].name());
            logStr.append(", ");
        }
        logStr.append(types[types.length - 1].name());
        logStr.append(" not tracking...");

        return logStr.toString();
    }
}
