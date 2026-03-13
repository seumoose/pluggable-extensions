# Pluggable Extensions

Playground repository for experimenting around with multi-module Maven setup and the ServiceLoader class in Java.

## Table of Contents

- [Instillation](#instillation)
- [Further Instillation & Configuration](#further-instillation--configuration)
- [About](#about)

## Instillation

This project uses Maven and is setup to compile to Java 23 (requiring a JDK 23 flavour) - both of these can be installed from their respective websites, or, if using a package manager i.e. homebrew on macOS, installed with `brew install maven` and `brew install openjdk@23` respectively.

## Further Instillation & Configuration

This project sets version files for both `jenv` Java version and `mvnvm` maven version for ease of use found in the `.java-version` and `mvnvm.properties` files. Both of these version managers can be installed from their respective websites, or, if using a package manager i.e. homebrew on macOS, installed with `brew install jenv` and `brew install mvnvm` respectively.

jenv needs further shell configuration which can be found in their GitHub repository [here](https://github.com/jenv/jenv?tab=readme-ov-file#12-configuring-your-shell) at which point the Java version downloaded earlier can be added with the `jenv add` command i.e. `jenv add /opt/homebrew/opt/openjdk@23`.

## About

This is a PoC project that looks to use the `ServiceLoader` class to dynamically load and register custom plugins without any direct hardcoding required. Program starts at [ApplicationLauncher.java](launcher/src/main/java/com/seumoose/launcher/ApplicationLauncher.java).
