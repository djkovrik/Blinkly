#!/bin/bash

set -eu

if [ "${BLINKLY_ADS_BUILD_TYPE:-}" != "RELEASE" ]; then
    exit 0
fi

for variable_name in BLINKLY_ACHIEVEMENTS_AD_UNIT_ID BLINKLY_GARDEN_AD_UNIT_ID; do
    value="${!variable_name:-}"
    if [ -z "$value" ]; then
        echo "error: $variable_name must not be empty in Release"
        exit 1
    fi
    if [ "$value" = "demo-banner-yandex" ]; then
        echo "error: $variable_name must not use the Yandex demo ad unit in Release"
        exit 1
    fi
    if [[ "$value" != R-M-* ]]; then
        echo "error: $variable_name is not a Yandex production ad unit ID"
        exit 1
    fi
done
