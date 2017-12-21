#!/bin/bash

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
pushd ${DIR}/.. > /dev/null

MANIFEST_FILE='android/AndroidManifest.xml'
echo "Updating Android build information. New version code: ${TRAVIS_BUILD_NUMBER}";
sed -i.bak 's/android:versionCode="."/android:versionCode="'${TRAVIS_BUILD_NUMBER}'"/g' ${MANIFEST_FILE}

popd > /dev/null