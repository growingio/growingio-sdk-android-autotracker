-keepnames class * extends android.view.View
-keepnames class * extends android.app.Fragment
-keepnames class * extends android.support.v4.app.Fragment
-keepnames class * extends androidx.fragment.app.Fragment
-keep class android.support.v4.view.ViewPager{
    *;
}
-keep class android.support.v4.view.ViewPager$**{
    *;
}
-keep class androidx.viewpager.widget.ViewPager{
    *;
}
-keep class androidx.viewpager.widget.ViewPager$**{
    *;
}

-keep class * extends com.growingio.android.sdk.LibraryGioModule
-keep class * extends com.growingio.android.sdk.GeneratedGioModule
-keep class com.growingio.android.sdk.Generated** {*;}