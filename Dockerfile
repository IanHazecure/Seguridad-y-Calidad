FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/gamer-events-0.0.1-SNAPSHOT.jar app.jar

ENV DB_URL=jdbc:h2:file:/app/data/gamer_events
ENV DB_USERNAME=sa
ENV DB_PASSWORD=
ENV DB_DRIVER=org.h2.Driver

EXPOSE 8080

VOLUME ["/app/data"]

ENTRYPOINT ["java", "-jar", "app.jar"]
