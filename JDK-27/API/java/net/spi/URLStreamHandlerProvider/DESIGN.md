# java.net.spi.URLStreamHandlerProvider 设计文档

## 1. API 功能

为自定义 URL 协议提供 `URLStreamHandler` 的 SPI 抽象类，实现 `URLStreamHandlerFactory`。

- 抽象方法：`URLStreamHandler createURLStreamHandler(String protocol)`（按协议返回 handler，不支持返回 null）。
- 构造器：protected 无参构造器。

## 2. Java 用户如何使用

- 实现类：继承并实现 `createURLStreamHandler`，经 `META-INF/services` 注册，供 `URL` 解析自定义协议时调用。
- 组合链路：`provider.createURLStreamHandler("myproto")` → 返回对应 handler 或 null。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/net/spi/URLStreamHandlerProvider.java`）：
  该 SPI 桥接 `URLStreamHandlerFactory`，为 `URL` 提供协议处理扩展点。

## 4. 测试用例设计 + 自动化实现

文件：`URLStreamHandlerProviderTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / abstract / 实现 `URLStreamHandlerFactory` / protected 无参构造器 / `createURLStreamHandler` 方法面。
- 黑盒：匿名实现 → `instanceof URLStreamHandlerFactory` → 已知协议非 null、未知协议 null。
