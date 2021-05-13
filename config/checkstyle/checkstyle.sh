./gradlew :growingio-tracker:checkstyle \
&& ./gradlew :growingio-tracker-core:checkstyle \
&& ./gradlew :growingio-autotracker:checkstyle \
&& ./gradlew :growingio-autotracker-core:checkstyle \
&& ./gradlew :growingio-autotracker-gradle-plugin:checkstyle \
&& ./gradlew :growingio-annotation:checkstyle \
&& ./gradlew :growingio-annotation:compiler:checkstyle \
&& ./gradlew :inject-annotation:checkstyle \
&& ./gradlew :inject-annotation:compiler:checkstyle \
&& ./gradlew :demo:checkstyle \
&& ./gradlew :demo-core:checkstyle \
&& ./gradlew :demo-autotrack:checkstyle