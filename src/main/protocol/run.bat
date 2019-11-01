protoc  ./chat.proto --java_out=../java
protoc --descriptor_set_out=chat.desc chat.proto
