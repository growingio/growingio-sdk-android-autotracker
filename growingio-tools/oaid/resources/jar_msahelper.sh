#!/bin/bash

#
#   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

# 用于生成 msahelper.jar 的编译工具，步骤：
# 1. 确保你的 ANDROID_HOME 环境变量设置和对应的Android api是否存在
# 2. 确保 growingio-tracker-core 和 oaid 下的build已经存在
# 3. ./jar_msahelper.sh
ANDROID_API=android-31
ANDROID_JAR_CLASSPATH="${ANDROID_HOME}/platforms/$ANDROID_API/android.jar"
SDK_CORE=../../../growingio-tracker-core/build/intermediates/compile_library_classes_jar/debug/classes.jar
IOAID=../build/intermediates/compile_library_classes_jar/debug/classes.jar

if [ ! -d "classes" ];then
  mkdir "classes"
fi

javac -d classes/ -classpath $ANDROID_JAR_CLASSPATH:miit_mdid_1.0.10.jar:$SDK_CORE:$IOAID OaidHelper1010.java -Xlint:unchecked
javac -d classes/ -classpath $ANDROID_JAR_CLASSPATH:msa_mdid_1.0.13.jar:$SDK_CORE:$IOAID OaidHelper1013.java -Xlint:unchecked
javac -d classes/ -classpath $ANDROID_JAR_CLASSPATH:oaid_sdk_1.0.25.jar:$SDK_CORE:$IOAID OaidHelper1025.java -Xlint:unchecked
javac -d classes/ -classpath $ANDROID_JAR_CLASSPATH:oaid_sdk_1.1.0.jar:$SDK_CORE:$IOAID OaidHelper1100.java -Xlint:unchecked

cd classes
jar cvf ../../libs/msa_helper.jar ./com/growingio/android/oaid/*