# Pluggable Extensions

Playground repository for experimenting with multi-module Maven setup and the `ServiceLoader` class in Java.

## Table of Contents

- [About](#about)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Building](#building)
- [Running](#running)

## About

A proof-of-concept project that uses Java's `ServiceLoader` to dynamically discover and register plugin implementations at runtime — no hardcoded references required. Application entry point is [ApplicationLauncher.java](launcher/src/main/java/com/seumoose/launcher/ApplicationLauncher.java).

Plugins are grouped into families, each representing a type of capability (e.g. a weather plugin family). A family can have multiple variants, each configured independently to target different endpoints, retry limits, etc. At startup, variant configuration files are read from disk, merged with per-family defaults, and used to initialise each plugin instance.

The plugins are implementable either by the example [functional plugin interfaces](/core/src/main/java/com/seumoose/core/interfaces/) or by the [functional plugin abstract classes](/core/src/main/java/com/seumoose/core/spi/), the later of which validates correct typing at runtime and prevents `ClassCastException` errors.

## Prerequisites

- **JDK 25** — install from [the official site](https://jdk.java.net/) or via Homebrew: `brew install openjdk@25`
- **Maven 3** — install from [the official site](https://maven.apache.org/) or via Homebrew: `brew install maven`

### Optional version managers

This project includes version files for [`jenv`](https://github.com/jenv/jenv) (`.java-version`) and [`mvnvm`](https://github.com/mvnvm/mvnvm) (`mvnvm.properties`). Both can be installed via Homebrew:

```bash
brew install jenv mvnvm
```

`jenv` requires additional [shell configuration](https://github.com/jenv/jenv?tab=readme-ov-file#12-configuring-your-shell), after which the installed JDK can be registered with:

```bash
jenv add /opt/homebrew/opt/openjdk@25
```

## Configuration

The application reads plugin configuration files from a root directory. The path is resolved in order of precedence:

1. **JVM system property** — `-DCONFIGURATION_ROOT_PATH=<path>`
2. **Environment variable** — `CONFIGURATION_ROOT_PATH=<path>`
3. **Default** — `~/config`

The path must point to an existing directory. Each plugin implementation (family) has its own subdirectory containing a `defaults.json` (shared base values) file and one or more variant files that are merged on top of the default plugin configuration values when read in. An example of such can be found in the [Extension A defaults](/config/ExtensionA/defaults.json).

## Building

From the project root:

```bash
mvn clean install
```

This produces a thin jar for the launcher along with all dependency jars in `launcher/target/lib/`.

(optionally) to build the extension outside the classpath [Extension C](/extensions/extension-c/) from the project root and copy the jar to the `plugins` directory:

```bash
mkdir -p ./plugins && mvn clean install -f ./extensions/extension-c/pom.xml && cp ./extensions/extension-c/target/extension-c-1.0-SNAPSHOT.jar ./plugins
```

This produces a thin jar for the external extension simulating runtime loaded JARs compiled externally to the project.

## Running

### CLI

```bash
java -DCONFIGURATION_ROOT_PATH="$(pwd)/config" -DPLUGIN_ROOT_PATH="$(pwd)/plugins" -jar launcher/target/launcher-1.0-SNAPSHOT.jar
```

### VS Code

The [launch configuration](.vscode/launch.json) sets `CONFIGURATION_ROOT_PATH` to the workspace `config/` and `PLUGIN_ROOT_PATH` to the workspace `plugins/` (if relevant) directories respectively. Open the Run and Debug panel (`⇧⌘D`), select **Java Application**, and press `F5`.
