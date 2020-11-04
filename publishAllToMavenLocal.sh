#!/bin/bash

export IS_EXCLUDE_DEMOS=true
./gradlew clean \
&& ./gradlew :growingio-autotracker-gradle-plugin:publishReleaseAgentPublicationToMavenLocal \
&& ./gradlew :growingio-tracker-core:publishReleaseAgentPublicationToMavenLocal \
&& ./gradlew :growingio-tracker:publishReleaseAgentPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker-core:publishReleaseAgentPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker:publishReleaseAgentPublicationToMavenLocal \
&& ./gradlew clean \
&& export IS_EXCLUDE_DEMOS=false