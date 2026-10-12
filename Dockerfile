# Étape 1 : Compiler l'application avec Maven et Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xmx384m"
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Exécuter l'application avec Java 21
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -Xmx320m -Xms200m -XX:MaxMetaspaceSize=128m -jar app.jar --server.port=${PORT:-8080}"]