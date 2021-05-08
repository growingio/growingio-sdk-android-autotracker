#!/bin/bash

export IS_EXCLUDE_DEMOS=true
./gradlew clean \
&& ./gradlew :growingio-autotracker-gradle-plugin:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-annotation:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-annotation:compiler:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :integration:okhttp3:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-tracker-core:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-tracker:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker-core:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew clean \
&& export IS_EXCLUDE_DEMOS=false