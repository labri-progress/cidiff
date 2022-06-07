# Breakage test dataset

This is a small dataset of pass/fail build logs.

## Layout

Each directory represents a single breakage. It contains the following files:
- `pass.log`: a passing build log.
- `fail.log`: a failing build log.
- `groundtruth`: the list of lines containing the relevant error messages. 
- The first line is the parsing option corresponding to the logs. Available options are:
`DEFAULT`
`RAW_GITHUB`
`FULL_GITHUB`
- The format of the list of lines is `34-45` for an interval or `56` for a single line. Intervals and lines must be disjoint. 
- A letter, either `E` or `C` is at the beginning of each line. A line starting with `E (Error)` is an interval or a single line of an error message. It can be immediately followed by a line starting with `C (Context)`. Those lines might be helpful to better understand the error as they can provide additional information.

Example of a groundtruth file:

```
DEFAULT
E 34-45
C 33
E 56
```