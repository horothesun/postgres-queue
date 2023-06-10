#!/bin/bash

./scripts/test_db_up.sh

sbt integration/test
TESTS_EXIT_CODE="$?"

./scripts/test_db_down.sh

exit "${TESTS_EXIT_CODE}"
