```sh
git clone https://github.com/dapengzhang0/grpc-java
cd grpc-java
git checkout  test-gradle-dependency-analyze
./gradlew :grpc-core:build -PskipCodegen=true -PskipAndroid=true
```