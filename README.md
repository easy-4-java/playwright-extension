# playwright-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-21-orange)](https://github.com/easy-4-java/playwright-extension) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

playwright-extension is a thin, pooling-oriented utility layer on top of the official

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

**playwright-extension** is a thin, pooling-oriented utility layer on top of the official
[Microsoft Playwright](https://playwright.dev/java/) Java library (package namespace
`com.microsoft.playwright.spring.boot`). Its core idea is to reuse expensive Playwright resources through an
object pool instead of launching a browser per operation:

- **`BrowserContextPool`** — a `commons-pool2` `GenericObjectPool<BrowserContext>` that hands out pooled
  browser contexts (with optional `AbandonedConfig` eviction);
- **`BrowserContextPooledObjectFactory`** — creates/destroys contexts, supports normal launch and persistent
  launch (with a user-data directory), and keeps track of the owning `Playwright` instances;
- convenience utilities for cookies, sliders, page closing, browser-type selection and named thread pools.

| Is                                                        | Is not                                  |
| :-------------------------------------------------------- | :-------------------------------------- |
| A pooling + utility helper around Playwright for Java     | A reimplementation of Playwright        |
| Spring-friendly naming/package conventions (`spring.boot`) | A Spring Boot starter (no auto-config)  |
| Headless-friendly by default                              | A testing framework or runner           |

Typical scenarios:

| Scenario                     | Description                                                        |
| :--------------------------- | :------------------------------------------------------------------ |
| Scraping / automation        | Borrow a context, drive pages, return the context to the pool       |
| Screenshot services          | Reuse pooled contexts for repeated screenshot jobs                  |
| Persistent-profile sessions  | Keep login state via a user-data directory (persistent launch)      |

## 2. Features & Status

| Capability                                          | Status      | Main API                                                              |
| :-------------------------------------------------- | :---------- | :-------------------------------------------------------------------- |
| Browser context pooling                             | Implemented | `BrowserContextPool` (extends `GenericObjectPool<BrowserContext>`)    |
| Pool configuration                                  | Implemented | `BrowserContextPoolConfig` (commons-pool2 settings + `toPoolConfig()`) |
| Context factory (normal launch)                     | Implemented | `BrowserContextPooledObjectFactory(PlaywrightBrowserType, LaunchOptions, NewContextOptions)`; defaults: headless, 1920 x 1080 |
| Context factory (persistent launch)                 | Implemented | `BrowserContextPooledObjectFactory(PlaywrightBrowserType, LaunchPersistentContextOptions, String userDataRootDir)`; defaults to `java.io.tmpdir` |
| Cookie / slider / page utilities                    | Implemented | `PlaywrightUtil` (`getCookies`, `clearLocalStorage`, `slide`, `closePage`, ...) |
| Browser install trigger                             | Implemented | `PlaywrightInstall` — sets `PLAYWRIGHT_DOWNLOAD_HOST` and borrows once to trigger the download |
| Shutdown hook                                       | Implemented | `PlaywrightHook` (thread `playwright-shutdown-hook`) closes the factory |
| Thread factories/pools with names                   | Implemented | `ThreadUtils` (named daemon factories, fixed/scheduled pools)          |
| Typed option holders                                | Implemented | `BrowserLaunchOptions`, `BrowserNewContextOptions`, `PageScreenshotOptions`, ... + `OptionMapper` |
| Unit tests                                          | Not present | No `src/test` sources on this branch                                  |

## 3. Requirements & Compatibility

| Requirement        | Version                    |
| :----------------- | :------------------------- |
| JDK                | 8+                         |
| Maven              | 3.0+ (wrapper included)    |
| Playwright         | 1.46.0                     |
| commons-pool2      | 2.11.1                     |
| spring-core        | 5.2.15.RELEASE (runtime utils) |

Version lines of the easy4j project:

| Branch        | JDK  | Version pattern | Notes                       |
| :------------ | :--- | :-------------- | :-------------------------- |
| `feature/1.0.x` | 8    | `1.0.x.*`       | This README, current branch |
| `feature/2.0.x` | 17   | `2.0.x.*`       | JDK 17 line                 |
| `feature/3.0.x` | 21   | `3.0.x.*`       | JDK 21 line                 |

## 4. Architecture & Modules

```text
   application
        |
        v
 BrowserContextPool (GenericObjectPool<BrowserContext>)
        |
        v
 BrowserContextPooledObjectFactory
   |                       |
   | normal launch         | persistent launch (+userDataRootDir)
   v                       v
 Playwright+Browser       Playwright+Browser
 +NewContext (1920x1080)   +LaunchPersistentContext
        |                       |
        +-----------> BrowserContext <-----------+
                     |
                     v
        Page (cookies / slider / screenshot utils)
                     |
                     v
        return / destroy -> PlaywrightHook (shutdown)
```

Single-module Maven project (`jar` packaging), root package `com.microsoft.playwright.spring.boot`:

| Package            | Responsibility                                      |
| :----------------- | :-------------------------------------------------- |
| `pool`             | `BrowserContextPool`, `BrowserContextPoolConfig`, `BrowserContextPooledObjectFactory` |
| `options`          | Typed Playwright option holders + `OptionMapper`    |
| `hooks`            | `PlaywrightHook`, `PlaywrightInstall`               |
| `utils`            | `PlaywrightUtil`, `ThreadUtils`, `JmxBeanUtils`     |
| `exception`        | `PlaywrightException`                               |

## 5. Installation

Artifacts are published to the aliyun repository and GitHub Releases; they are **not** on Maven Central yet.

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>playwright-extension</artifactId>
    <version>3.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

```groovy
implementation 'io.github.easy4j:playwright-extension:3.0.x.x.20260630-SNAPSHOT'
```

## 6. Quick Start

```java
// 1) Factory: chromium, headless launch, default 1920 x 1080 context
BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
        PlaywrightBrowserType.chromium, null, null);

// 2) Pool
BrowserContextPool pool = new BrowserContextPool(factory);

// 3) Use
BrowserContext context = pool.borrowObject();
try {
    Page page = context.newPage();
    page.navigate("https://example.com");
    System.out.println(page.title());
} finally {
    pool.returnObject(context);
}

// 4) Shutdown
factory.close(); // closes all pooled Playwright instances
```

Expected result: the first `borrowObject()` downloads/launches the browser (ensure `mvn exec:java` runs after
`playwright install`, or use `PlaywrightInstall` to trigger it), the page title is printed, and the context
returns to the pool for reuse.

## 7. Configuration

| Setting                     | How                                                                  | Default                |
| :-------------------------- | :------------------------------------------------------------------- | :--------------------- |
| Browser type                | `PlaywrightBrowserType` enum (`chromium` / `firefox` / `webkit`)     | `chromium`             |
| Launch options              | `BrowserType.LaunchOptions` (e.g. `setHeadless(true)`)               | headless               |
| Context options             | `Browser.NewContextOptions`                                          | screen 1920 x 1080     |
| Persistent launch           | `BrowserType.LaunchPersistentContextOptions` + `userDataRootDir`     | `java.io.tmpdir`       |
| Pool size / eviction        | `BrowserContextPoolConfig` (maxTotal/maxIdle/minIdle/eviction policy, ...) | commons-pool2 defaults |
| Download host               | `PlaywrightInstall(browserContextPool, downloadHost)` → `PLAYWRIGHT_DOWNLOAD_HOST` | —            |

## 8. Core Usage / API

Utilities (`PlaywrightUtil`):

```java
// slide a slider to the right (e.g. for captcha widgets)
PlaywrightUtil.slide(page, ".slide-verify", 260, 30);

// cookies as a string (name=value; name2=value2)
String cookies = PlaywrightUtil.getCookies(page);

// pick the BrowserType for a Playwright instance
BrowserType browserType = PlaywrightUtil.getBrowserType(playwright, PlaywrightBrowserType.webkit);
```

Named threads (`ThreadUtils`):

```java
ExecutorService pool = ThreadUtils.newThreadPoolExecutor(2, 4, 60,
        TimeUnit.SECONDS, "playwright-worker", true);
```

Option mapping (`OptionMapper`) — apply values only when present:

```java
// apply `value` to the consumer only when it is non-null / has text
OptionMapper.get().from(value).whenHasText().to(options::setLocale);
OptionMapper.get().from(viewport).whenNonNull().to(options::setViewportSize);
```

Error handling: Playwright failures surface as `PlaywrightException` (a `RuntimeException` with error code /
description).

## 9. Testing & Build

```bash
./mvnw clean verify
```

- Maven wrapper (`mvnw`) is committed to the repository.
- JaCoCo is configured with a line-coverage rule of 90% (`haltOnFailure=false`).
- There are no unit tests on this branch — the coverage gate is not effectively enforced yet (known gap).
  Pool borrow/return and utility tests would be the most valuable additions.

## 10. Versioning & Branches

| Branch        | JDK  | Version pattern | Maintenance                          |
| :------------ | :--- | :-------------- | :----------------------------------- |
| `feature/1.0.x` | 8    | `1.0.x.*`       | Current branch (Playwright 1.46.x)   |
| `feature/2.0.x` | 17   | `2.0.x.*`       | JDK 17 line                          |
| `feature/3.0.x` | 21   | `3.0.x.*`       | JDK 21 line                          |

Artifacts are distributed via the aliyun Maven repository and GitHub Releases. Use the branch matching your
JDK baseline.

## 11. Contributing & License

Contributions are welcome — especially unit tests and additional pooling scenarios. Please open an issue
before larger changes.

This project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
