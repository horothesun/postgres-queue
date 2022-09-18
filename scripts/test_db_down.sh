#!/bin/bash

TEST_CONTAINER="test-postgres"

echo "Shutting Postgres down..."
docker kill "${TEST_CONTAINER}"
