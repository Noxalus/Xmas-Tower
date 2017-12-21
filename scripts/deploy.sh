#!/bin/bash

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
pushd ${DIR}/.. > /dev/null

SIGNED_APK="XmasTower.apk"

echo "Deploy signed APK on HockeyApp"
echo "Commit message: $TRAVIS_COMMIT_MESSAGE"
echo "Signed APK: $SIGNED_APK"

curl -F "status=2" -F "notify=1" -F "notes=$TRAVIS_COMMIT_MESSAGE" -F "notes_type=0" -F "ipa=@$SIGNED_APK" -H "X-HockeyAppToken: $HOCKEYAPP_TOKEN" https://rink.hockeyapp.net/api/2/apps/upload

popd > /dev/null