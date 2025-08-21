FROM openjdk:21-slim

WORKDIR /app

COPY build/libs/freelanceAPI-1.0.jar /app/freelanceAPI.jar

ENTRYPOINT ["java","-jar","freelanceAPI.jar"]