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

# Citation

If you CiDiff in an academic work, we would love if you cite the following article:
- [**What Happened in This Pipeline? Diffing Build Logs with CiDiff**](https://arxiv.org/abs/2504.18182): Nicolas Hubner, Jean-Rémy Falleri, Raluca Uricaru, Thomas Degueule, Thomas Durieux. ISSTA, 2025.
