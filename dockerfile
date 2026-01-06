FROM eclipse-temurin:17
ADD target/demo-0.0.1-SNAPSHOT.jar demoe-0.0.1-SNAPSHOT.jar
ENTRYPOINT [ "java","-jar","demo-0.0.1-SNAPSHOT.jar" ]
