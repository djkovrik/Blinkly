#!/bin/bash

set -eu

if [ "${BLINKLY_ADS_BUILD_TYPE:-}" != "RELEASE" ]; then
    exit 0
fi

if [ "${BLINKLY_IOS_ADS_RELEASE_READY:-NO}" != "YES" ]; then
    echo "error: iOS ads release gate is closed. Activate the iOS app in Yandex, create Banner units, replace both placeholder IDs, and set BLINKLY_IOS_ADS_RELEASE_READY = YES"
    exit 1
fi

for variable_name in BLINKLY_ACHIEVEMENTS_AD_UNIT_ID BLINKLY_GARDEN_AD_UNIT_ID; do
    value="${!variable_name:-}"
    if [ -z "$value" ]; then
        echo "error: $variable_name must not be empty in Release"
        exit 1
    fi
    if [[ "$value" == REPLACE_BEFORE_IOS_RELEASE_* ]]; then
        echo "error: $variable_name is still an iOS release placeholder"
        exit 1
    fi
    if [ "$value" = "demo-banner-yandex" ]; then
        echo "error: $variable_name must not use the Yandex demo ad unit in Release"
        exit 1
    fi
    if [[ ! "$value" =~ ^R-M-[0-9]+-[0-9]+$ ]]; then
        echo "error: $variable_name is not a valid Yandex production ad unit ID"
        exit 1
    fi
done

if [ "$BLINKLY_ACHIEVEMENTS_AD_UNIT_ID" = "$BLINKLY_GARDEN_AD_UNIT_ID" ]; then
    echo "error: iOS Achievements and Garden must use distinct Yandex Banner ad unit IDs"
    exit 1
fi

achievements_app_prefix="${BLINKLY_ACHIEVEMENTS_AD_UNIT_ID%-*}"
garden_app_prefix="${BLINKLY_GARDEN_AD_UNIT_ID%-*}"
if [ "$achievements_app_prefix" != "$garden_app_prefix" ]; then
    echo "error: iOS Achievements and Garden ad unit IDs must belong to the same Yandex app"
    exit 1
fi
