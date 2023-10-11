#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

current_branch=$(git rev-parse --abbrev-ref HEAD)
if [ "$current_branch" != "gh-pages" ]; then
  echo "Not on gh-pages branch"
  exit 1
fi

if date --version >/dev/null 2>&1 ; then
  # GNU date
  cut_off_date="$(date --date="1 month ago" +%s)000"
else
  # BSD date
  cut_off_date="$(date -v -1m +%s)000"
fi

(
  echo -n 'window.BENCHMARK_DATA = '
  cat dev/bench/data.js | \
    sed 's|window.BENCHMARK_DATA = ||' | \
    jq '{
      "lastUpdate": .lastUpdate,
      "repoUrl": ."repoUrl",
      "entries": {
        "JMH Benchmark": [
          .entries."JMH Benchmark".[] | select((.date >= '${cut_off_date}') or (.commit.url | contains("https://github.com/zio/zio-kafka/commit")))
        ]
      }
    }' \
) > dev/bench/data.js2

cat dev/bench/data.js2 > dev/bench/data.js
rm dev/bench/data.js2
