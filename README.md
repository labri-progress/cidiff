# CiDiff

Awesome differ for CI build logs

## Installation

### From source

Clone and compile the repository.

```bash
git clone https://github.com/labri-progress/cidiff.git
cd cidiff
./gradlew build
```

You'll have a binary in the folder `cidiff/build/install/cidiff/bin/`.

## Usage

The command line is defined as such:

```bash
cidiff LEFT_LOG_FILE RIGHT_LOG_FILE -o KEY VALUE -o KEY VALUE
```

With `LEFT_LOG_FILE` the path the original log file and `RIGHT_LOG_FILE` the path to the modified log file.

Options can be provided after these two mandatory arguments using the following syntax: `-o KEY VALUE`.

Available options are described when you run the program without any arguments

## Benchmark

1. modifier le chemin du benchmark dans la variable `DATASET` dans le fichier `src/test/java/org/github/cidiff/benchmark/Benchmark.java`
2. `./gradlew benchmark` pour executer le benchmark
3. le résultat est dans le fichier `build/reports/benchmark-florent-timeout.csv`

