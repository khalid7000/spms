# StratAlign backend API image, built for RIT Dubai's Academic Dashboard platform-join
# convention (Developer Guide §6.6): a standalone container joined to the platform's own
# Docker network, with no public host port of its own -- the platform's edge (Caddy) is the
# only thing that ever talks to it, over that shared network, at the API image/service name
# `strtalign-api` (see docker-compose.rit.yml alongside this file).

# ---- build stage --------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

# pom.xml's maven-toolchains-plugin requires a matching <toolchains><jdk> entry in the
# invoking user's toolchains.xml -- this image only has one JDK (21, the same version the
# plugin asks for), so point toolchains straight at the image's own JAVA_HOME rather than
# shipping a separate JDK just to satisfy the plugin.
RUN mkdir -p /root/.m2 && \
    printf '<?xml version="1.0" encoding="UTF-8"?>\n<toolchains>\n  <toolchain>\n    <type>jdk</type>\n    <provides><version>21</version></provides>\n    <configuration><jdkHome>%s</jdkHome></configuration>\n  </toolchain>\n</toolchains>\n' "$JAVA_HOME" > /root/.m2/toolchains.xml

# Dependency layer cached separately from source so a source-only change doesn't
# re-download the world.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package && \
    mv target/spms-*.jar target/app.jar

# ---- runtime stage -------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Actuator health checks below need curl; also gives RIT's own orchestration something to
# exec into the container with if needed.
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

RUN groupadd --system strtalign && useradd --system --gid strtalign --home /app strtalign
WORKDIR /app
COPY --from=build /build/target/app.jar app.jar
RUN mkdir -p /app/uploads && chown -R strtalign:strtalign /app
USER strtalign

# Documentation only -- Docker EXPOSE doesn't publish a host port by itself. Per the platform
# convention, this container must NOT get a `ports:` mapping in compose; it's reached only via
# the shared Docker network at this port.
EXPOSE 8085

# Only vars with a real, safe container-level default go in ENV below. Everything else (DB_URL,
# DB_USERNAME, DB_PASSWORD, JWT_SECRET, PLATFORM_JWT_SECRET, BOOTSTRAP_ADMIN_EMAIL,
# BOOTSTRAP_ADMIN_PASSWORD, GATEWAY_SSO_*, ANTHROPIC_API_KEY) is deliberately left UNSET here --
# each already has a dev-only fallback via Spring's ${VAR:default} in application.yml, and
# setting an ENV to "" would count as a *present* empty value to Spring, not "unset", which
# would defeat that fallback rather than trigger it. Supply the real ones via the compose
# `environment:`/`secrets:` at deploy time instead.
ENV SERVER_ADDRESS=0.0.0.0 \
    UPLOADS_DIR="/app/uploads"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
