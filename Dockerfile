# ---- Étape de build : compilation avec Maven sur JDK Temurin 25 ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
# Copie du pom puis des sources pour profiter du cache de couches Docker
COPY pom.xml .
COPY src ./src
# Package sans relancer les tests (déjà exécutés en CI)
RUN mvn -DskipTests clean package

# ---- Étape d'exécution : JRE Temurin 25 minimal ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
