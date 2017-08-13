# Gradle Autoversion Plugin

## Introduction

This plugin provides automatic versioning for your Gradle projects and is intended for projects that adopt continuous delivery principles where every commit (at least to master) is a release candidate, and struggle with more traditional versioning systems such as semantic versioning.

## Installation

To install this plugin, add the following to the `buildscript` section at the top of your `build.gradle`:

```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.github.mixja:autoversion:0.1.0'
    }
}

apply plugin: 'com.github.mixja.autoversion'
```

Once installed, the plugin will automatically set the `version` property in your project, and adds a task called `version`:

```
$ gradle version -q
17.06.18

# All versioned artefacts will be versioned according to the generated version
$ gradle build
...
...

```

## Versioning Configuration

The current versioning behaviour uses the following values based from the current commit to generate a version:

`<year-of-latest-commit>.<month-of-latest-commit>.<number-of-commits-in-commit-month>`

> All date calculations are based on UTC time

For example, if the latest commit was June 18th, 2017 and there have been 12 commits since June 1st, 2017, the generated version would be:

`17.06.12`

### Build Identifiers

The versioning scheme supports an optional build identifer, which by default is the value of the `BUILD_ID` environment variable if present:

```
$ export BUILD_ID=build1356
$ gradle version -q
17.06.12.build1356
```

You can also configure your own expressions for obtaining the build identifier in your `build.gradle` file:

```
autoVersion {
	buildVersion = System.getenv("MY_BUILD_SYSTEM_ID")
}

```

### Branch Versioning

By default, the generated version does not include branch information if the current branch is `master`.  

This behaviour can be changed by configuring the `defaultBranch` property:

```
autoVersion {
	buildVersion = System.getenv("MY_BUILD_SYSTEM_ID")
	defaultBranch = develop
}

```

If a non-default branch is detected, the branch name will be included in the generated version as follows:

```
$ git checkout my-feature-branch
$ gradle version -q
17.06.12.my-feature-branch
$ export BUILD_ID=build1111
$ gradle version -q
17.06.12.my-feature-branch.build1111
```


