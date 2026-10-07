#!/usr/bin/env bash
# Fails when a removed risk shows up again in source, manifests, or Gradle files.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

fail() {
  echo "release hygiene: $1" >&2
  exit 1
}

scan() {
  grep -RInE \
    --include='*.kt' --include='*.kts' --include='*.xml' --include='*.properties' \
    --exclude-dir='.git' --exclude-dir='build' --exclude-dir='.gradle' \
    "$1" . && return 0
  return 1
}

if scan 'openai-android-sdk|org\.jsoup|play-services-location|Html\.fromHtml|WebView|addJavascriptInterface|MODE_WORLD_READABLE'; then
  fail "risky API or dependency reappeared"
fi

if scan 'usesCleartextTraffic="true"|android:allowBackup="true"|android\.permission\.INTERNET|ACCESS_FINE_LOCATION|ACCESS_COARSE_LOCATION'; then
  fail "network, location, backup, or cleartext setting reappeared"
fi

if scan 'AKIA[0-9A-Z]{16}|AIza[0-9A-Za-z_-]{35}|sk-proj-|sk-ant-'; then
  fail "possible credential found"
fi

if grep -nE '\.[0-9]*\+' app/build.gradle.kts >/dev/null; then
  fail "dynamic dependency version in app/build.gradle.kts"
fi

if ! grep -q 'targetSdk = 36' app/build.gradle.kts; then
  fail "targetSdk must stay at 36 for Play submissions"
fi

if ! grep -q 'cleartextTrafficPermitted="false"' app/src/main/res/xml/network_security_config.xml; then
  fail "cleartext must stay disabled"
fi

echo "release hygiene: ok"
