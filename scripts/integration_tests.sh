#!/bin/bash

./scripts/test_db_up.sh
sbt IntegrationTest/test
./scripts/test_db_down.sh
