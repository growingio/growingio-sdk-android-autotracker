#!/bin/bash

export IS_EXCLUDE_DEMOS=true
./gradlew clean \
&& ./gradlew :growingio-autotracker-gradle-plugin:publishOfficialPublicationToMavenLocal \
&& ./gradlew :growingio-tracker:publishOfficialPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker:publishOfficialPublicationToMavenLocal \
&& ./gradlew clean \
&& export IS_EXCLUDE_DEMOS=false