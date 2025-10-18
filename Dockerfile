FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon clean build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]
