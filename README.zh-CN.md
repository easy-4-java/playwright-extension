# playwright-extension

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-8-orange)](https://github.com/easy-4-java/playwright-extension) [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

playwright-extension 是基于官方 Microsoft Playwright Java 库（包命名空间 com.microsoft.playwright.spring.boot）的轻量池化工具层。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 功能与状态](#2-功能与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本线与分支](#10-版本线与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述

**playwright-extension** 是基于官方 [Microsoft Playwright](https://playwright.dev/java/) Java 库（包命名空间 `com.microsoft.playwright.spring.boot`）的轻量池化工具层。核心思路是通过对象池复用昂贵的 Playwright 资源，而不是每次操作都启动浏览器：

- **`BrowserContextPool`** —— 基于 `commons-pool2` 的 `GenericObjectPool<BrowserContext>`，提供池化的浏览器上下文（支持可选的 `AbandonedConfig` 驱逐）；
- **`BrowserContextPooledObjectFactory`** —— 创建/销毁上下文，支持普通启动与持久化启动（带用户数据目录），并跟踪所属的 `Playwright` 实例；
- 便捷工具：Cookie、滑块、页面关闭、浏览器类型选择与命名线程池。

| 是                                                      | 不是                                  |
| :------------------------------------------------------ | :------------------------------------ |
| Playwright Java 的池化与工具辅助层                       | Playwright 的重新实现                 |
| 采用 Spring 友好命名/包约定（`spring.boot`）             | Spring Boot Starter（无自动配置）     |
| 默认无头模式友好                                        | 测试框架或运行器                       |

典型场景：

| 场景               | 说明                                                        |
| :----------------- | :---------------------------------------------------------- |
| 爬取 / 自动化      | 借出上下文、驱动页面、归还上下文到池                        |
| 截图服务           | 复用池化上下文处理批量截图任务                              |
| 持久化会话         | 通过用户数据目录（持久化启动）保留登录状态                  |

## 2. 功能与状态

| 能力                                                | 状态       | 主要 API                                                              |
| :-------------------------------------------------- | :--------- | :-------------------------------------------------------------------- |
| 浏览器上下文池化                                    | 已实现     | `BrowserContextPool`（继承 `GenericObjectPool<BrowserContext>`）      |
| 池配置                                              | 已实现     | `BrowserContextPoolConfig`（commons-pool2 配置 + `toPoolConfig()`）   |
| 上下文工厂（普通启动）                              | 已实现     | `BrowserContextPooledObjectFactory(PlaywrightBrowserType, LaunchOptions, NewContextOptions)`；默认无头、1920 x 1080 |
| 上下文工厂（持久化启动）                            | 已实现     | `BrowserContextPooledObjectFactory(PlaywrightBrowserType, LaunchPersistentContextOptions, String userDataRootDir)`；默认 `java.io.tmpdir` |
| Cookie / 滑块 / 页面工具                            | 已实现     | `PlaywrightUtil`（`getCookies`、`clearLocalStorage`、`slide`、`closePage` 等） |
| 浏览器安装触发                                      | 已实现     | `PlaywrightInstall` —— 设置 `PLAYWRIGHT_DOWNLOAD_HOST` 并借出一次以触发下载 |
| 关闭钩子                                            | 已实现     | `PlaywrightHook`（线程名 `playwright-shutdown-hook`）关闭工厂          |
| 命名线程工厂/线程池                                  | 已实现     | `ThreadUtils`（命名守护线程工厂、固定/定时线程池）                    |
| 类型化选项持有类                                     | 已实现     | `BrowserLaunchOptions`、`BrowserNewContextOptions`、`PageScreenshotOptions` 等 + `OptionMapper` |
| 单元测试                                            | 暂无       | 本分支 `src/test` 无测试源码                                            |

## 3. 环境要求与兼容性

| 要求          | 版本                     |
| :------------ | :----------------------- |
| JDK           | 8                        |
| Maven         | 3.0+（已内置 wrapper）    |
| Playwright    | 1.46.0                   |
| commons-pool2 | 2.12.0                   |
| spring-core   | 5.3.39（运行时工具）      |

easy4j 项目的版本线：

| 分支           | JDK  | 版本模式   | 说明                            |
| :------------- | :--- | :--------- | :------------------------------ |
| `feature/1.0.x` | 8    | `1.0.x.*`  | 本文档对应分支                   |
| `feature/2.0.x` | 17   | `2.0.x.*`  | JDK 17 版本线                   |
| `feature/3.0.x` | 21   | `3.0.x.*`  | JDK 21 版本线                   |

## 4. 架构与模块

```text
   应用
        |
        v
 BrowserContextPool (GenericObjectPool<BrowserContext>)
        |
        v
 BrowserContextPooledObjectFactory
   |                       |
   | 普通启动              | 持久化启动 (+userDataRootDir)
   v                       v
 Playwright+Browser       Playwright+Browser
 +NewContext (1920x1080)   +LaunchPersistentContext
        |                       |
        +-----------> BrowserContext <-----------+
                     |
                     v
        Page (cookies / 滑块 / 截图 工具)
                     |
                     v
        归还 / 销毁 -> PlaywrightHook (关闭)
```

单模块 Maven 项目（`jar` 打包），根包 `com.microsoft.playwright.spring.boot`：

| 包            | 职责                                            |
| :------------ | :---------------------------------------------- |
| `pool`        | `BrowserContextPool`、`BrowserContextPoolConfig`、`BrowserContextPooledObjectFactory` |
| `options`     | 类型化 Playwright 选项持有类 + `OptionMapper`   |
| `hooks`       | `PlaywrightHook`、`PlaywrightInstall`           |
| `utils`       | `PlaywrightUtil`、`ThreadUtils`、`JmxBeanUtils` |
| `exception`   | `PlaywrightException`                           |

## 5. 安装

制品发布在阿里云私服与 GitHub Releases，**尚未发布到 Maven Central**。

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

## 6. 快速开始

```java
// 1) 工厂：chromium，无头启动，默认 1920 x 1080 上下文
BrowserContextPooledObjectFactory factory = new BrowserContextPooledObjectFactory(
        PlaywrightBrowserType.chromium, null, null);

// 2) 池
BrowserContextPool pool = new BrowserContextPool(factory);

// 3) 使用
BrowserContext context = pool.borrowObject();
try {
    Page page = context.newPage();
    page.navigate("https://example.com");
    System.out.println(page.title());
} finally {
    pool.returnObject(context);
}

// 4) 关闭
factory.close(); // 关闭所有池化 Playwright 实例
```

预期结果：首次 `borrowObject()` 会下载/启动浏览器（请先执行 `playwright install`，或使用 `PlaywrightInstall` 触发），随后打印页面标题，上下文归还池中复用。

## 7. 配置

| 配置项               | 设置方式                                                              | 默认值               |
| :------------------- | :-------------------------------------------------------------------- | :------------------- |
| 浏览器类型           | `PlaywrightBrowserType` 枚举（`chromium` / `firefox` / `webkit`）     | `chromium`           |
| 启动选项             | `BrowserType.LaunchOptions`（例如 `setHeadless(true)`）               | 无头                 |
| 上下文选项           | `Browser.NewContextOptions`                                           | 屏幕 1920 x 1080     |
| 持久化启动           | `BrowserType.LaunchPersistentContextOptions` + `userDataRootDir`      | `java.io.tmpdir`     |
| 池大小 / 驱逐        | `BrowserContextPoolConfig`（maxTotal/maxIdle/minIdle/驱逐策略等）      | commons-pool2 默认值 |
| 下载主机             | `PlaywrightInstall(browserContextPool, downloadHost)` → `PLAYWRIGHT_DOWNLOAD_HOST` | —          |

## 8. 核心用法 / API

工具类（`PlaywrightUtil`）：

```java
// 拖动滑块（例如验证码组件）
PlaywrightUtil.slide(page, ".slide-verify", 260, 30);

// Cookie 转字符串（name=value; name2=value2）
String cookies = PlaywrightUtil.getCookies(page);

// 按枚举选择 BrowserType
BrowserType browserType = PlaywrightUtil.getBrowserType(playwright, PlaywrightBrowserType.webkit);
```

命名线程（`ThreadUtils`）：

```java
ExecutorService pool = ThreadUtils.newThreadPoolExecutor(2, 4, 60,
        TimeUnit.SECONDS, "playwright-worker", true);
```

选项映射（`OptionMapper`）——仅在值存在时应用：

```java
// 值非空/有文本时才将其应用到 consumer
OptionMapper.get().from(value).whenHasText().to(options::setLocale);
OptionMapper.get().from(viewport).whenNonNull().to(options::setViewportSize);
```

错误处理：Playwright 失败以 `PlaywrightException`（携带错误码/描述信息的 `RuntimeException`）形式抛出。

## 9. 测试与构建

```bash
./mvnw clean verify
```

- 仓库内置 Maven wrapper（`mvnw`）。
- 已配置 JaCoCo 行覆盖率 90% 门禁（`haltOnFailure=false`）。
- 本分支没有单元测试——覆盖率门禁实际上未被有效执行（已知缺口）。池借还与工具类的测试是最有价值的补充。

## 10. 版本线与分支

| 分支           | JDK  | 版本模式   | 维护说明                              |
| :------------- | :--- | :--------- | :------------------------------------ |
| `feature/1.0.x` | 8    | `1.0.x.*`  | 当前分支（Playwright 1.46.x）         |
| `feature/2.0.x` | 17   | `2.0.x.*`  | JDK 17 版本线                         |
| `feature/3.0.x` | 21   | `3.0.x.*`  | JDK 21 版本线                         |

制品通过阿里云 Maven 私服与 GitHub Releases 分发。请按 JDK 基线选择对应分支。

## 11. 贡献与许可

欢迎贡献——尤其是单元测试与更多池化场景。较大改动请先提交 issue 讨论。

本项目基于 [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) 许可。
