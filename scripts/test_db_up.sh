#!/bin/bash

export PGPASSWORD="test_pwd"

TEST_USERNAME="postgres"
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
  postgres

echo "Waiting for Postgres to be ready..."
sleep 3

echo "Creating DB..."
psql \
  --host localhost \
  --port "${TEST_DB_PORT}" \
  --username "${TEST_USERNAME}" \
  --command "CREATE DATABASE ${TEST_DB_NAME};"
