FROM gradle:8.4.0-jdk21-alpine AS build
LABEL authors="Senko-san"
LABEL authors="AgniaEndie"
LABEL authors="GekkStr"
LABEL autors="xxlegendzxx22"
WORKDIR /earthbridge
COPY . /earthbridge
ENTRYPOINT ["java","-XX:+UseZGC", "-jar", "/mars-earth-listener/build/libs/mars-earth-listener-1.0-SNAPSHOT.jar"]
