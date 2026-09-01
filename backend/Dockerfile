FROM eclipse-temurin:21-jre-alpine

RUN apk --no-cache add curl jq

RUN addgroup -S spring && adduser -S spring -G spring

RUN mkdir -p /var/log &&\
    mkdir -p /opt/app &&\
    mkdir -p /builder


RUN chown -R spring:spring /var/log &&\
    chown -R spring:spring /opt/app &&\
    chown -R spring:spring /builder

WORKDIR /opt/app

USER spring:spring

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","app.jar"]