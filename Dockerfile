FROM openjdk:8-jre-alpine
COPY target/docker-0.0.1-SNAPSHOT.jar /usr/src/blabla/
WORKDIR /usr/src/blabla/
CMD ["java", "-jar", "docker-0.0.1-SNAPSHOT.jar"]