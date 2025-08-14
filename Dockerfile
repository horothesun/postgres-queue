# The only purpose of this file is to get Renovate updates on
# - Postgres Docker image version and
# - Temurin JDK version.
#
# Integration tests use the following POSTGRES_VERSION value (through `scripts/get_postgres_version.sh`).
# The CI uses the following JAVA_VERSION value to configure its JDK setup (through `scripts/get_java_version.sh`).

ARG POSTGRES_VERSION=17.6-alpine

ARG JAVA_VERSION=21

FROM postgres:${POSTGRES_VERSION} as psql

FROM eclipse-temurin:${JAVA_VERSION} as jdk
