# --- Etapa 1: Compilación ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- Etapa 2: Ejecución ---
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
# Copiamos el JAR gordo generado omitiendo los .jar estandar si los hubiera
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
