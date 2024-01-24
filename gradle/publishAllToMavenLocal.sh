#!/bin/bash

./gradlew clean \
&& ./gradlew :growingio-annotation:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-annotation:compiler:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-tracker-core:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker-core:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-data:database:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-data:json:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-data:protobuf:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-data:encoder:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-network:okhttp3:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-network:urlconnection:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-hybrid:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-ads:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-abtest:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-webservice:debugger:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-webservice:circler:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-apm:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-flutter:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-tools:platform:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-tools:oaid:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :gio-sdk:tracker:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :gio-sdk:autotracker:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :gio-sdk:autotracker-saas:publishMavenAgentPublicationToMavenLocal \
&& ./gradlew :growingio-sdk-bom:publishBomPublicationToMavenLocal \
&& ./gradlew clean