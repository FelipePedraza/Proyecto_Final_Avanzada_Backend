#
# Etapa de construcción
#
FROM gradle:8.7-jdk21 AS build
USER gradle
WORKDIR /home/gradle/project

# Copiar solo archivos necesarios primero para aprovechar el cache de dependencias
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
RUN gradle --no-daemon build -x test || return 0

# Copiar el resto del código fuente
COPY --chown=gradle:gradle . .

# Construir el archivo JAR
RUN gradle --no-daemon bootJar

#
# Etapa de empaquetado
#
FROM eclipse-temurin:21-jre
ENV APP_HOME=/app
WORKDIR ${APP_HOME}

# Argumento opcional para puerto
ARG PORT=8080
ENV PORT=${PORT}

# Copiar el JAR generado desde la etapa de construcción
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE ${PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]