FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/SalonApp-0.0.1-SNAPSHOT.jar"]
