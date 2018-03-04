#./gradlew :mvp:clean
#./gradlew :mvp:assemble
#./gradlew :mvp:sourcesJar
#./gradlew :mvp:javadocJar
#./gradlew :mvp:install
#./gradlew :mvp:bintrayUpload

./gradlew :mvp:clean && ./gradlew :mvp:build && ./gradlew :mvp:sourcesJar && ./gradlew :mvp:javadocJar && ./gradlew :mvp:generatePomFileForCorePublication && ./gradlew :mvp:install && ./gradlew :mvp:bintrayUpload
