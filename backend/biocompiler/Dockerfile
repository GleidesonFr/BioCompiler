FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

EXPOSE 8080

CMD ["java", "-jar", "target/biocompiler-0.0.1-SNAPSHOT.jar"]
