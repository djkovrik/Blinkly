#!/bin/bash

set -euo pipefail

if [ "${CONFIGURATION:-}" != "Release" ]; then
    exit 0
fi

api_key="${BLINKLY_APPMETRICA_API_KEY:-}"
uuid_pattern='^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'

if [[ ! "$api_key" =~ $uuid_pattern ]]; then
    echo "error: Release AppMetrica API key must be a non-placeholder UUID" >&2
    exit 1
fi
