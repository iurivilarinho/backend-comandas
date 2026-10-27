# syntax=docker/dockerfile:1.4
#
# Own-instance image (CENTRAL_MODE=off). The hosted deployment (comandas.gloomy.com.br) uses the generic Dockerfile.app
# of aurora-platform/infra instead.
#
# The platform starter is built from source at the git tag v${PLATFORM_VERSION} (no package registry): the first stage
# clones aurora-platform over SSH with the ssh-agent of the build host (BuildKit SSH forwarding) and runs `mvn install`.
#
#   eval "$(ssh-agent -s)"; ssh-add /root/.ssh/aurora-platform-deploy
#   docker compose -p comandas build --ssh default
#
# platform stage: the starter
FROM maven:3.9-eclipse-temurin-17 AS platform
ARG PLATFORM_VERSION=0.7.0
ARG PLATFORM_REPO=git@github.com:iurivilarinho/aurora-platform.git
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,sharing=locked \
    command -v git >/dev/null && command -v ssh >/dev/null || \
    (apt-get update && apt-get install -y --no-install-recommends git openssh-client)
RUN --mount=type=ssh \
    --mount=type=cache,target=/root/.m2/repository \
    set -eu; \
    mkdir -p -m 700 /root/.ssh; \
    ssh-keyscan -t ed25519,rsa github.com >> /root/.ssh/known_hosts 2>/dev/null; \
    git clone --depth 1 --branch "v${PLATFORM_VERSION}" "${PLATFORM_REPO}" /platform-src; \
    cd /platform-src/backend; \
    mvn -B -q -DskipTests -pl platform-starter -am install; \
    mkdir -p /out/m2/br/com/gloomy; \
    cp -r /root/.m2/repository/br/com/gloomy/platform /out/m2/br/com/gloomy/

# build stage: the app against that starter
FROM maven:3.9-eclipse-temurin-17 AS build
ARG PLATFORM_VERSION=0.7.0
WORKDIR /app
COPY --from=platform /out/m2 /opt/platform-m2
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository \
    cp -r /opt/platform-m2/. /root/.m2/repository/ && \
    mvn -B -DskipTests -Dplatform.version="${PLATFORM_VERSION}" package

# runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 9091
ENTRYPOINT ["java","-jar","/app/app.jar"]
