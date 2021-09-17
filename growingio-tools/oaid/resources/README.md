## 通过如下命令编译对应版本的OaidHelper类用于获取oaid

```shell
# 编译各个版本的OaidHelper
javac -d . -cp ~/Library/Android/sdk/platforms/android-30/android.jar:./oaid_sdk_1.0.26.jar:/Users/lijh/Downloads/growingio-sdk-android-autotracker/growingio-tracker-core/build/intermediates/runtime_library_classes/debug/classes.jar ../src/main/java/com/growingio/android/oaid/IOaidHelper.java OaidHelper1026.java 

javac -d . -cp ~/Library/Android/sdk/platforms/android-30/android.jar:./oaid_sdk_1.0.25.jar:/Users/lijh/Downloads/growingio-sdk-android-autotracker/growingio-tracker-core/build/intermediates/runtime_library_classes/debug/classes.jar ../src/main/java/com/growingio/android/oaid/IOaidHelper.java OaidHelper1025.java 

javac -d . -cp ~/Library/Android/sdk/platforms/android-30/android.jar:./msa_mdid_1.0.13.jar:/Users/lijh/Downloads/growingio-sdk-android-autotracker/growingio-tracker-core/build/intermediates/runtime_library_classes/debug/classes.jar ../src/main/java/com/growingio/android/oaid/IOaidHelper.java OaidHelper1013.java 

javac -d . -cp ~/Library/Android/sdk/platforms/android-30/android.jar:./miit_mdid_1.0.10.jar:/Users/lijh/Downloads/growingio-sdk-android-autotracker/growingio-tracker-core/build/intermediates/runtime_library_classes/debug/classes.jar ../src/main/java/com/growingio/android/oaid/IOaidHelper.java OaidHelper1010.java 

# 打包为msa_helper.jar
jar cvf msa_helper.jar *
```


