#!/bin/bash

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
pushd ${DIR}/.. > /dev/null

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 HOCKEYAPP_TOKEN" >&2
  exit 1
fi

HOCKEYAPP_TOKEN=$1
SIGNED_APK="XmasTower.apk"

echo "Deploy signed APK on HockeyApp"
echo "HockeyApp Token: $HOCKEYAPP_TOKEN"
echo "Commit message: $TRAVIS_COMMIT_MESSAGE"
echo "Signed APK: $SIGNED_APK"

curl -F "status=2" -F "notify=1" -F "notes=$TRAVIS_COMMIT_MESSAGE" -F "notes_type=0" -F "ipa=@$SIGNED_APK" -H "X-HockeyAppToken: $HOCKEYAPP_TOKEN" https://rink.hockeyapp.net/api/2/apps/upload

popd > /dev/null