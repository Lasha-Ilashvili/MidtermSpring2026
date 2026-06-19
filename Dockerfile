FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --gid 10001 uno \
    && useradd --uid 10001 --gid uno --no-create-home --shell /usr/sbin/nologin uno

COPY --from=build --chown=uno:uno /workspace/target/uno-cli.jar /app/uno-cli.jar
COPY --chown=uno:uno docker/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

USER uno

ENV SPRING_PROFILES_ACTIVE=production

ENTRYPOINT ["/app/entrypoint.sh"]
CMD ["demo"]
