protoc  ./sample.proto --java_out=../java
protoc --descriptor_set_out=sample.desc sample.proto

pause