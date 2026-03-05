#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_PROPS="$ROOT_DIR/local.properties"

SDK="${ANDROID_SDK_ROOT:-}"
if [[ -z "$SDK" ]]; then
  if [[ "$OSTYPE" == "darwin"* ]]; then
    SDK="$HOME/Library/Android/sdk"
  else
    SDK="$HOME/Android/Sdk"
  fi
fi

if [[ ! -d "$SDK" ]]; then
  echo "Android SDK directory not found: $SDK" >&2
  echo "Install it in Android Studio (SDK Manager) or export ANDROID_SDK_ROOT." >&2
  exit 1
fi

echo "sdk.dir=$SDK" > "$LOCAL_PROPS"
echo "Wrote $LOCAL_PROPS"
echo "sdk.dir=$SDK"
