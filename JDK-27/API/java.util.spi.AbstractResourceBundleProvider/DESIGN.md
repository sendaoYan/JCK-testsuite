# java.util.spi.AbstractResourceBundleProvider 测试方案

白盒结构断言在 `AbstractResourceBundleProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- `public abstract class`，实现 `ResourceBundleProvider`。
- 构造器：`protected AbstractResourceBundleProvider()`（等价 `("java.properties")`）与 `protected AbstractResourceBundleProvider(String...)`；后者校验 formats 非空且只能为 `"java.class"`/`"java.properties"`（空数组/未知值抛 `IllegalArgumentException`，null 抛 NPE）。
- `toBundleName(String,Locale)`：protected，等于 `ResourceBundle.Control.getControl(FORMAT_DEFAULT).toBundleName`。
- `getBundle(String,Locale)`：public，用 `toBundleName` 得到的名字在本 provider 模块内查找资源包，找不到返回 `null`；null 参数抛 NPE。

## 2. Java 用户如何使用

- 子类继承并调用 `toBundleName` 生成包名（`com.foo` + `Locale.US` → `com.foo_en_US`）。
- `getBundle` 默认从模块内加载 bundle；跨模块/未提供时返回 `null` 表示不提供。

## 3. 开源代码实际用法调研

> WebSearch 不可用，基于 `~/git/jdk25u-dev` 源码。

- `src/java.base/share/classes/java/util/ResourceBundle.java:1509-1510`：`CONTROL_PROVIDERS.stream().flatMap(provider -> Stream.ofNullable(provider.getControl(baseName))).findFirst()`——同族 SPI 的流式 provider 组合。
- 类文档自带示例（`AbstractResourceBundleProvider.java:65-76`）：子类覆写 `toBundleName` 为 `"p." + language + "." + baseName`，并在 `getBundle` 里按 locale 返回 `super.getBundle` 或 `null`。

## 4. 测试用例设计 + 自动化实现

- **正常**：`toBundleName` 对 ROOT/US/FRENCH/zh_CN 的精确拼串。
- **getBundle 缺省**：无名模块内找不到包 → `null`。
- **构造器**：`java.properties`/`java.class` 合法；空数组/未知格式 → `IllegalArgumentException`；null formats → NPE。
- **边界/异常**：`getBundle(null, locale)`、`getBundle(base, null)` → NPE。
- **自动化**：`AbstractResourceBundleProviderTest.testBlackBox()`，headless 无 `@key`。
