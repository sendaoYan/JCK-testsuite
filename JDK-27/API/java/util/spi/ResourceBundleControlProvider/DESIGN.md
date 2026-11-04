# java.util.spi.ResourceBundleControlProvider 测试方案

白盒结构断言在 `ResourceBundleControlProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- `public interface`：`ResourceBundle.Control getControl(String baseName)`。
- 语义：返回用于加载 `baseName` 资源包的 `Control`，或 `null` 表示不干预。

## 2. Java 用户如何使用

- 实现返回固定 `Control`（如 `ResourceBundle.Control.getNoFallbackControl(FORMAT_DEFAULT)`），或按 base name 返回 `null`。
- JDK 内部用 `ServiceLoader` 收集 provider，逐个 `getControl` 直到第一个非 null（`ResourceBundle.java:1509-1510`）。

## 3. 开源代码实际用法调研

> WebSearch 不可用，基于 `~/git/jdk25u-dev` 源码。

- `src/java.base/share/classes/java/util/ResourceBundle.java:1500-1510`：`ServiceLoader.load(ResourceBundleControlProvider.class, ...)` 收集后 `stream().flatMap(provider -> Stream.ofNullable(provider.getControl(baseName))).findFirst()`。

## 4. 测试用例设计 + 自动化实现

- **正常**：`getControl("foo")` 返回固定引用；`getControl("bar")` 返回 `null`。
- **真实性**：返回的 `Control` 是真实 `Control`（`getFormats` 返回 `FORMAT_DEFAULT`）。
- **自动化**：`ResourceBundleControlProviderTest.testBlackBox()`，headless 无 `@key`。
