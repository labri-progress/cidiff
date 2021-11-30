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

With `LEFT_LOG_FILE` the path the original log file and `RIGHT_LOG_FILE` the path to the modified logf file.

Options can be provided after these two mandatory arguments using the following syntax: `-o KEY VALUE`.

Available options are:

* `parser`: `CLASSIC` or `GITHUB` (default: `CLASSIC`)
* `parser.classic.timestampSize`: any integer greater or equals to 0 (default: `0`)
* `differ.updated`: `true` or `false` (default: `false`)
* `differ.added`: `true` or `false` (default: `true`)
* `differ.deleted`: `true` or `false` (default: `true`)