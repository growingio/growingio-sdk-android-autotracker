#!/bin/bash

./gradlew clean \
&& ./gradlew :growingio-annotation:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-annotation:compiler:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-tracker-core:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-autotracker-core:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-data:database:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-data:json:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-data:protobuf:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-data:encoder:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-network:okhttp3:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-network:urlconnection:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-hybrid:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-ads:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-abtest:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-webservice:debugger:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-webservice:circler:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-apm:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-flutter:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-compose:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-tools:platform:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-tools:oaid:publishMavenPublicationToMavenLocal \
&& ./gradlew :gio-sdk:tracker:publishMavenPublicationToMavenLocal \
&& ./gradlew :gio-sdk:tracker-cdp:publishMavenPublicationToMavenLocal \
&& ./gradlew :gio-sdk:autotracker:publishMavenPublicationToMavenLocal \
&& ./gradlew :gio-sdk:autotracker-cdp:publishMavenPublicationToMavenLocal \
&& ./gradlew :gio-sdk:autotracker-saas:publishMavenPublicationToMavenLocal \
&& ./gradlew :growingio-sdk-bom:publishMavenPublicationToMavenLocal \
&& ./gradlew clean