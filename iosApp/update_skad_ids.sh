#!/bin/bash

# Based on the updater published in the Yandex Mobile Ads SKAdNetwork guide.
set -o pipefail

readonly TAG="[YandexAds]"
readonly PLIST_BUDDY="/usr/libexec/PlistBuddy"
readonly PLIST_PATH="${SRCROOT}/${INFOPLIST_FILE}"
readonly URLS=(
    "https://yastatic.net/pcode-static/skadnetwork/skadids.json"
    "https://yastatic.net/pcode-static/skadnetwork/skadids.xml"
)

if [ ! -f "$PLIST_PATH" ]; then
    echo "warning: ${TAG} Info.plist not found at $PLIST_PATH; SKAdNetwork update skipped"
    exit 0
fi

response=""
for url in "${URLS[@]}"; do
    response=$(curl -sf --max-time 5 "$url" 2>/dev/null) && [ -n "$response" ] && break
    response=""
done

if [ -z "$response" ]; then
    echo "warning: ${TAG} SKAdNetwork sources are unavailable; update skipped"
    exit 0
fi

remote_ids=$(echo "$response" | grep -ioE '[a-z0-9]+\.skadnetwork' | tr '[:upper:]' '[:lower:]' | sort -u)
if [ -z "$remote_ids" ]; then
    echo "warning: ${TAG} No SKAdNetwork IDs were parsed; update skipped"
    exit 0
fi

existing_ids=$("$PLIST_BUDDY" -c "Print :SKAdNetworkItems" "$PLIST_PATH" 2>/dev/null \
    | grep -ioE '[a-z0-9]+\.skadnetwork' \
    | tr '[:upper:]' '[:lower:]' \
    | sort -u)

if [ -n "$existing_ids" ]; then
    ids_to_add=$(comm -23 <(echo "$remote_ids") <(echo "$existing_ids"))
else
    ids_to_add="$remote_ids"
fi

if [ -z "$ids_to_add" ]; then
    exit 0
fi

"$PLIST_BUDDY" -c "Print :SKAdNetworkItems" "$PLIST_PATH" >/dev/null 2>&1 \
    || "$PLIST_BUDDY" -c "Add :SKAdNetworkItems array" "$PLIST_PATH"

index=$("$PLIST_BUDDY" -c "Print :SKAdNetworkItems" "$PLIST_PATH" 2>/dev/null | grep -c "Dict")
while IFS= read -r skad_id; do
    [ -z "$skad_id" ] && continue
    "$PLIST_BUDDY" \
        -c "Add :SKAdNetworkItems: dict" \
        -c "Add :SKAdNetworkItems:${index}:SKAdNetworkIdentifier string ${skad_id}" \
        "$PLIST_PATH" >/dev/null 2>&1 || {
        echo "warning: ${TAG} Unable to add SKAdNetwork ID $skad_id"
        continue
    }
    index=$((index + 1))
done <<< "$ids_to_add"
