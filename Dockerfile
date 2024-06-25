FROM maven:3.9.7 AS build

WORKDIR /pico-server

COPY /pico-server/pom.xml .
COPY /pico-server/src ./src

RUN mvn clean package

FROM openjdk:21-jdk-slim

WORKDIR /pico-server

COPY --from=build /pico-server/target/* /pico/

CMD ["java", "-jar", "/pico/pico-server-1.0-SNAPSHOT-jar-with-dependencies.jar"]