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

-dontwarn com.google.android.material.button.MaterialButton
-dontwarn com.google.android.material.slider.RangeSlider
-dontwarn com.google.android.material.slider.Slider
-dontwarn com.google.android.material.tabs.TabLayout$Tab
-dontwarn com.google.android.material.tabs.TabLayout$TabView