
./gradlew :growingio-annotation:checkstyle \
&& ./gradlew :growingio-annotation:compiler:checkstyle \
&& ./gradlew :growingio-tracker-core:checkstyle \
&& ./gradlew :growingio-autotracker-core:checkstyle \
&& ./gradlew :growingio-data:json:checkstyle \
&& ./gradlew :growingio-data:protobuf:checkstyle \
&& ./gradlew :growingio-data:encoder:checkstyle \
&& ./gradlew :growingio-data:database:checkstyle \
&& ./gradlew :growingio-hybrid:checkstyle \
&& ./gradlew :growingio-network:okhttp3:checkstyle \
&& ./gradlew :growingio-network:urlconnection:checkstyle \
&& ./gradlew :growingio-network:volley:checkstyle \
&& ./gradlew :growingio-webservice:debugger:checkstyle \
&& ./gradlew :growingio-webservice:circler:checkstyle \
&& ./gradlew :growingio-apm:checkstyle \
&& ./gradlew :growingio-advert:checkstyle \
&& ./gradlew :growingio-tools:oaid:checkstyle \
&& ./gradlew :growingio-tools:snappy:checkstyle \
&& ./gradlew :gio-sdk:tracker-cdp:checkstyle \
&& ./gradlew :gio-sdk:autotracker-cdp:checkstyle \
&& ./gradlew :demo:checkstyle \
&& ./gradlew :demo-core:checkstyle \
&& ./gradlew :demo-autotrack:checkstyle