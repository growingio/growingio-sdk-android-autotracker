#!/bin/bash

export IS_EXCLUDE_DEMOS=true
./gradlew clean \
&& ./gradlew :growingio-autotracker-gradle-plugin:publishOfficialPublicationToMavenLocal \
&& ./gradlew :growingio-tracker:publishOfficialPublicationToMavenLocal \
&& ./gradlew :growingio-tracker-api:publishOfficialPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker:publishOfficialPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker-api:publishOfficialPublicationToMavenLocal \
&& ./gradlew clean \
&& export IS_EXCLUDE_DEMOS=false