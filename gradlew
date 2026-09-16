#!/usr/bin/env bash
set -eu

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "Gradle is not installed or not on PATH. Install Gradle 9.1.1 and try again." >&2
  exit 1
fi
