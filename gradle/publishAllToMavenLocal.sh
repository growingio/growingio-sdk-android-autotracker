#!/bin/bash

export IS_EXCLUDE_DEMOS=true
./gradlew clean \
&& ./gradlew :growingio-annotation:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-annotation:compiler:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-tracker-core:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker-core:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-data:json:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-data:encoder:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-network:okhttp3:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-network:urlconnection:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-network:volley:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-hybrid:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-webservice:debugger:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-webservice:circler:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-tools:crash:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :gio-sdk:tracker:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :gio-sdk:tracker-cdp:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :gio-sdk:autotracker:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :gio-sdk:autotracker-cdp:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker-gradle-plugin:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew clean
export IS_EXCLUDE_DEMOS=false