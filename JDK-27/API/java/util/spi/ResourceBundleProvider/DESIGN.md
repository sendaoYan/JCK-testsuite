# java.util.spi.ResourceBundleProvider 测试方案

白盒结构断言在 `ResourceBundleProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- `public interface`，函数式 SPI：`ResourceBundle getBundle(String baseName, Locale locale)`。
- 语义：返回给定 base name + locale 的资源包，或 `null` 表示该 provider 不提供。

## 2. Java 用户如何使用

- 用 lambda/匿名类实现：`(baseName, locale) -> "foo".equals(baseName) ? bundle : null`。
- 返回真实 `ResourceBundle`（如 `ListResourceBundle` 子类）供调用方 `getString`/`getObject` 取内容。

## 3. 开源代码实际用法调研

> WebSearch 不可用，基于 `~/git/jdk25u-dev` 源码。

- `src/java.base/share/classes/java/util/ResourceBundle.java` 中 `ResourceBundleProvider` 与 `AbstractResourceBundleProvider` 共同构成模块化资源包加载 SPI（见 `AbstractResourceBundleProvider.java:65-76` 示例）。
- `AbstractResourceBundleProvider` 是 JDK 提供的参考实现（`implements ResourceBundleProvider`）。

## 4. 测试用例设计 + 自动化实现

- **正常**：返回 `ListResourceBundle` 的引用，断言 `getString("k")`、`getObject("n")`、`containsKey`。
- **组合/边界**：非匹配 base name 返回 `null`；同一 provider 对不同 locale 返回同一 bundle 引用。
- **自动化**：`ResourceBundleProviderTest.testBlackBox()`，headless 无 `@key`。
