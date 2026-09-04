FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

ENV SERVER_PORT=10000
EXPOSE 10000

CMD ["sh", "-c", "java -jar target/biocompiler-0.0.1-SNAPSHOT.jar --server.port=${PORT:-10000}"]
