FROM eclipse-temurin:17.0.7_7-jre-alpine
ARG JAR_FILE=*signed.jar
ENV LOG_HOME=/opt/gcUnicorn/.log
RUN addgroup -S unicorn && adduser -S unicorn -G unicorn
RUN mkdir -p /opt/gcUnicorn/.log
RUN chown unicorn:unicorn /opt/gcUnicorn/.log
USER unicorn:unicorn
COPY ${JAR_FILE} /opt/gcUnicorn/gcUnicorn.jar
ENTRYPOINT ["java","-jar","/opt/gcUnicorn/gcUnicorn.jar"]
