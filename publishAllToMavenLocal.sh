#!/bin/bash

export IS_EXCLUDE_DEMOS=true
./gradlew clean \
&& ./gradlew :growingio-autotracker-gradle-plugin:publishReleasePublicationToMavenLocal \
&& ./gradlew :growingio-tracker:publishReleasePublicationToMavenLocal \
&& ./gradlew :growingio-tracker-api:publishReleasePublicationToMavenLocal \
&& ./gradlew :growingio-autotracker:publishReleasePublicationToMavenLocal \
&& ./gradlew :growingio-autotracker-api:publishReleasePublicationToMavenLocal \
&& ./gradlew clean \
&& export IS_EXCLUDE_DEMOS=false