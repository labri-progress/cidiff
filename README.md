# CIDiff

Awesome differ for CI build logs

## Installation

### From source

Clone and compile the repository.

~~~
git clone https://github.com/labri-progress/cidiff.git
cd cidiff
./gradlew build
~~~

You'll have a binary in the folder `cidiff/build/install/cidiff/bin/`.

## Usage

The command line is defined as such:

~~~
cidiff LEFT_LOG_FILE RIGHT_LOG_FILE -o KEY VALUE -o KEY VALUE
~~~

With `LEFT_LOG_FILE` the path the original log file and `RIGHT_LOG_FILE` the path to the modified log file.

Options can be provided after these two mandatory arguments using the following syntax: `-o KEY VALUE`.

Available options are:

### Parsing

* `parser`: parser type `CLASSIC`, `RAW_GITHUB` or `FULL_GITHUB` (default: `CLASSIC`)
* `parser.classic.timestampSize`: timestamp size (default: `0`)

### Diffing

* `differ`: `ALTERNATING_BRUTE_FORCE`, `BRUTE_FORCE`, `LCS`, `SEED_EXTEND` (default: `BRUTE_FORCE`)
* `differ.rewrite.min`: min rewrite similarity (default `0.5`)
* `differ.seed.block`: min block size for seed differ (default `3`)
* `differ.seed.window`: max window size for updated detection for seed differ (default `30`)

### Output

* `client`: client type `SWING`, `JSON`, `CONSOLE`, `METRICS`
* `console.updated`: show updated lines in console `true` or `false` (default: `false`)
* `console.added`: show added lines in console `true` or `false` (default: `true`)
* `console.deleted`: show deleted lines in console `true` or `false` (default: `true`)
* `console.unchanged`: show unchanged lines in console `true` or `false` (default: `false`)
