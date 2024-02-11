# The only purpose of this file is to get Renovate updates on the Temurin JDK version.
# The CI uses the following JAVA_VERSION value to configure its JDK setup (through `scripts/get_java_version.sh`).

ARG POSTGRES_VERSION=15.3-alpine3.18
ARG JAVA_VERSION=21

FROM postgres:${POSTGRES_VERSION} as psql

FROM eclipse-temurin:${JAVA_VERSION} as jdk
