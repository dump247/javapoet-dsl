#!/bin/bash

set -euf -o pipefail

# Store the current commit hash alongside the docs so we record what version the docs were generated from
LATEST_COMMIT=$(git rev-parse --verify HEAD)
echo "$LATEST_COMMIT" > build/javadoc/javapoet-dsl/commit

# Move style.css alongside the docs
mv build/javadoc/style.css build/javadoc/javapoet-dsl/
find build/javadoc -type f -name '*.html' -exec sed -i 's/\.\.\/style\.css/style.css/g' {} \;

# Switch to gh-pages branch
git checkout gh-pages
git clean -fdx -e build/javadoc

if [[ "${LATEST_COMMIT}" == "$(cat latest/commit)" ]]; then
  exit 0
fi

# Replace latest version files with updated docs
git rm -rf latest/
mv build/javadoc/javapoet-dsl latest/

# Commit the javadocs
git add latest/
git commit -a -m "New javadoc version from $LATEST_COMMIT"
git push