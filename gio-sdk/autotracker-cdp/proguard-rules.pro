# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# https://www.guardsquare.com/manual/configuration/usage

-dontoptimize
-dontshrink

#-------------- okhttp3 --------------
-keep class okhttp3.* { *;}
-keepclassmembernames class * implements okhttp3.* {
    public <methods>;
}

#-------------- 注入代码 --------------
-keepclasseswithmembernames class com.growingio.android.sdk.autotrack**Injector {
    public static <methods>;
}

#-------------- 对外接口 --------------
-keep class * extends com.growingio.android.sdk.GeneratedGioModule
-keep class * extends com.growingio.android.sdk.LibraryGioModule

-keep class com.growingio.android.sdk.GeneratedGioModuleImpl
-keep class * extends com.growingio.android.sdk.Tracker {
    public <methods>;
}
-keep class com.growingio.android.sdk.Tracker {
    public <methods>;
}
-keep class com.growingio.android.sdk.autotrack.GrowingAutotracker {
    public <methods>;
}
-keep class com.growingio.android.sdk.autotrack.CdpAutotrackConfiguration {
    public <methods>;
}
-keep class com.growingio.android.sdk.CoreConfiguration {
    public <methods>;
}
-keep class com.growingio.android.sdk.LibraryGioModule {
    public <methods>;
}
-keep class com.growingio.android.sdk.Configurable {
    public <methods>;
}
-keep class com.growingio.android.sdk.track.events.helper.EventExcludeFilter {
    public <fields>;
    public <methods>;
}
-keep class com.growingio.android.sdk.track.events.helper.FieldIgnoreFilter {
    public <fields>;
    public <methods>;
}
-keep enum com.growingio.android.sdk.autotrack.IgnorePolicy {
    *;
}

#-------------- 删除代码中Log相关的代码 --------------
-assumenosideeffects class android.util.Log {
    static *** isLoggable(...);
    static *** v(...);
    static *** i(...);
    static *** w(...);
    static *** d(...);
    static *** e(...);
    static *** wtf(...);
}

-assumenosideeffects class com.growingio.android.sdk.track.log.Logger {
    static ***  v(...);
    static ***  i(...);
    static ***  w(...);
    static ***  d(...);
    static ***  e(...);
    static ***  wtf(...);
}

# R8 支持的规则
# 参见 https://source.android.google.cn/devices/tech/debug/understanding-logging?hl=zh-cn
-maximumremovedandroidloglevel 0