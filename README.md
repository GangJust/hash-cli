# hash-cli

Minimal Kotlin CLI for generating per-file verification reports.

## Requirements

- Java 21+
- Gradle wrapper is included; use `./gradlew` or `gradlew.bat`

## Behavior

- Accepts one file or directory path
- Recursively processes directories
- Excludes any file ending with `_verification.txt`
- Writes one sibling output file per source file as `<original>_verification.txt`
- Overwrites existing output files
- By default writes `MD5`, `SHA1`, `SHA256`, `CRC32`
- Supports selecting algorithms with repeated `--algorithm`
- Supports uppercase hash output with `--uppercase`
- Writes UTF-8 text and keeps the selected algorithm order
- Reusable hash utility supports both `File` and `InputStream`

## Options

- `-a, --algorithm <md5|sha1|sha256|crc32>`
- `-u, --uppercase`
- `-h, --help`
- `-V, --version`

## Build

```powershell
.\gradlew.bat test
.\gradlew.bat fatJar
```

## Run

```powershell
java -jar .\build\libs\hash-cli.jar <file-or-directory-path>
java -jar .\build\libs\hash-cli.jar demo.apk --uppercase
java -jar .\build\libs\hash-cli.jar demo.apk -a md5 -a sha1
```
