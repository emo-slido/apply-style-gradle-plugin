# Plugin: a8c-apply-style-gradle-plugin

This plugin applies a common set of styling rules to an android project.

For now the plugin only configures [Detekt](https://github.com/detekt/detekt).

## Loading the plugin

You can apply the plugin to your project by doing the following:

**build.gradle**:

```groovy
plugins {
    id 'a8c-apply-style-gradle-plugin' version '0.0.1'
}
```

**build.gradle.kts**:

```kotlin
plugins {
    id("a8c-apply-style-gradle-plugin") version "0.0.1"
}
```

## Configuration

The plugin doesn't use any custom configuration.
Although we can still configure `detekt` through the standard gradle configuration block.

## Detekt

The plugin provides some sane defaults for `detekt` as well as a default configuration file.
This means that projects can adopt it without having to provide any settings to `detekt` in their gradle files.

### Baseline

If you want to provide a custom baseline for `detekt` you can do so by adding the file to the repo:

```file
${project.rootDir}/config/detekt/baseline.xml
```

**NOTE**: this is the default baseline file that `detekt`
uses thus you do not need to add any configuration directive to your `build.gradle` file..

If you want to use a different baseline file you will need to add the section `detekt` to your gradle file:

```groovy
detekt {
    baseline = file("settings/detekt/baseline.xml")
}
```

## Configuration file

If your project has deviated from the standard defaults that `detekt` provides and you need to use a custom configuration file you can.
By default `detekt` loads its configuration file from:

```file
${project.rootDir}/config/detekt/detekt.yml
```

**NOTE**: this is the default configuration file that `detekt`
uses thus you do not need to add any configuration directive to your `build.gradle` file.

If you change the location of the file you will need to add the section `detekt` to your gradle file:

```groovy
detekt {
    config  = files("settings/detekt/detekt.yml")
}
```

## Debug

You can get enhanced debug by enabling the environment variable `A8C_DEBUG`:

```bash
A8C_DEBUG=1 ./gradlew detekt
```
