# Étape 1 : Build de l'application (Compilation)
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Exécution (Image finale légère)
# On utilise eclipse-temurin qui est la norme actuelle pour Spring Boot
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]