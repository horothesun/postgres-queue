#!/bin/bash

POSTGRES_VERSION=$(grep "ARG POSTGRES_VERSION=" "Dockerfile" | grep -o "[^=]*$")

[[ -z "${POSTGRES_VERSION}" ]] && echo "Error: POSTGRES_VERSION not found in Dockerfile" && exit 123

export POSTGRES_VERSION
