# Pruning the benchmarks report page

Benchmarks are run on every commit, even in pull requests. This causes the historic graphs on
https://zio.github.io/zio-kafka/dev/bench/ to contain so many results that it becomes unreadable. Rather than limiting
the number of items in the graph (which is supported by the github action that generates the page), we keep the
benchmark results of _all_ master commits, but only keep pull request commits for a limited time (currently 1 week).

The process is currently manual. Here are the instructions:

* Make sure `jq` is installed
* Checkout the `gh-pages` branch
* Execute the following command in the root of the project:
  ```shell
  scripts/prune-benchmark-history.sh
  ```
* Commit and push changes
