```sh
git clone https://github.com/dapengzhang0/grpc-java
cd grpc-java
git checkout  test-gradle-dependency-analyze-1.4.1
./gradlew :grpc-gae-interop-testing:build -PskipCodegen=true -PskipAndroid=true
```
