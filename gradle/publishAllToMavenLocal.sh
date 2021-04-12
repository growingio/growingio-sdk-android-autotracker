#!/bin/bash

export IS_EXCLUDE_DEMOS=true
./gradlew clean \
&& ./gradlew :growingio-autotracker-gradle-plugin:publishToMavenLocal \
&& ./gradlew :growingio-tracker-core:publishToMavenLocal \
&& ./gradlew :growingio-tracker:publishToMavenLocal \
&& ./gradlew :growingio-autotracker-core:publishToMavenLocal \
&& ./gradlew :growingio-autotracker:publishToMavenLocal \
&& ./gradlew clean \
&& export IS_EXCLUDE_DEMOS=false