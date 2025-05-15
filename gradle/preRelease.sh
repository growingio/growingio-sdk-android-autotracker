#!/bin/bash

# 用于 Sdk Demo 的 UI测试

test_sdk_version='growingio = "4.0.0"'
test_plugin_version='growingioPlugin = "4.0.0"'

test_sdk_version=$(grep "^growingio =.*" gradle/libs.versions.toml | awk -F= '{print}')
test_plugin_version=$(grep "^growingioPlugin =.*" gradle/libs.versions.toml | awk -F= '{print}')

# echo $test_sdk_version
# echo $test_plugin_version

# for macOS
# sed -i "" "s/^growingioPlugin =.*/${test_plugin_version}/g" demo/gradle/libs.versions.toml
# sed -i "" "s/^growingio =.*/${test_sdk_version}/g" demo/gradle/libs.versions.toml

# for linux
sed -i "s/^growingioPlugin =.*/${test_plugin_version}/g" demo/gradle/libs.versions.toml
sed -i "s/^growingio =.*/${test_sdk_version}/g" demo/gradle/libs.versions.toml

# echo 'apply from: "${rootProject.projectDir.parent}/gradle/jacoco.gradle"' >> demo/app/build.gradle

