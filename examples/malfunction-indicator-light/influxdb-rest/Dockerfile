FROM gcr.io/distroless/java:11

COPY build/libs/influxdb-rest-?.?.?.jar /app.jar

EXPOSE 8080

CMD ["app.jar"]