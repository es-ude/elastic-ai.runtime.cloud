FROM openjdk:17-alpine
LABEL authors=IES-UDE
LABEL description="Monitor for the elastic-AI ecosystem"
LABEL homepage="https://www.uni-due.de/es/en/en_elastic_ai.php"

RUN addgroup monitor && \
    adduser --disabled-password --ingroup monitor monitor

WORKDIR /usr/src/monitor
COPY build/libs/monitor.jar monitor.jar

USER monitor

EXPOSE 80/tcp

CMD ["java", "-jar", "monitor.jar"]
