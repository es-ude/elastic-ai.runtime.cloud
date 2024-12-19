FROM openjdk:17-alpine

RUN addgroup monitor && \
    adduser --disabled-password --ingroup monitor monitor

WORKDIR /usr/src/monitor
COPY build/libs/monitor.jar monitor.jar

USER monitor

EXPOSE 80

CMD["java", "-jar", "monitor.jar"]