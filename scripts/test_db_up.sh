#!/bin/bash

[[ -z "${POSTGRES_VERSION}" ]] && echo "Error: POSTGRES_VERSION not defined" && exit 123

export PGPASSWORD="test_pwd"

TEST_DEFAULT_USER="postgres"
TEST_DB_HOST="localhost"
TEST_CONTAINER="test-postgres"
TEST_DB_NAME="test_db"
TEST_DB_PORT="5432"

echo "Spinning Postgres up..."
docker run \
  --rm \
  --detach \
  --name "${TEST_CONTAINER}" \
  --publish "${TEST_DB_PORT}:${TEST_DB_PORT}" \
  --env POSTGRES_PASSWORD="${PGPASSWORD}" \
  "postgres:${POSTGRES_VERSION}"

echo "Waiting for Postgres to be ready..."
sleep 3

echo "Creating DB..."
psql \
  --host "${TEST_DB_HOST}" \
  --port "${TEST_DB_PORT}" \
  --username "${TEST_DEFAULT_USER}" \
  --command "CREATE DATABASE ${TEST_DB_NAME};"

echo "Applying schema..."
psql "${TEST_DB_NAME}" \
  --host "${TEST_DB_HOST}" \
  --port "${TEST_DB_PORT}" \
  --username "${TEST_DEFAULT_USER}" \
  --file scripts/schema.sql
