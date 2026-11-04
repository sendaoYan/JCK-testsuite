# java.util.prefs.PreferencesFactory 测试方案

白盒结构断言在 `PreferencesFactoryTest.testWhiteBox()`，黑盒行为断言在 `PreferencesFactoryTest.testBlackBox()`。

## 1. API 功能

- 接口方法：`Preferences systemRoot()`、`Preferences userRoot()`。
- 服务提供者接口（SPI）：`Preferences` 通过系统属性 `java.util.prefs.PreferencesFactory` 或 `ServiceLoader` 定位实现，返回用户/系统两棵根节点。

## 2. Java 用户如何使用

- 实现 `PreferencesFactory` 返回自定义 `Preferences`（通常继承 `AbstractPreferences`），再以 `-Djava.util.prefs.PreferencesFactory=com.YourFactory` 注入。
- 工厂方法应返回同一实例（`userRoot()` 幂等），且 `userRoot()` 的 `isUserNode()==true`、`systemRoot()` 的 `isUserNode()==false`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`。

- `src/java.prefs/share/classes/java/util/prefs/Preferences.java:220-243`：`factory()` 先读系统属性 `java.util.prefs.PreferencesFactory`，`Class.forName(...).newInstance()` 实例化；否则 `ServiceLoader`，最后回退平台默认实现。
- `test/jdk/java/util/prefs/PrefsSpiTest.java:85`：`-Djava.util.prefs.PreferencesFactory=StubPreferencesFactory` 注入测试工厂。
- `test/jdk/java/util/prefs/StubPreferencesFactory.java`：官方 SPI 桩实现，返回 `StubPreferences`（继承 `AbstractPreferences`）。

## 4. 测试用例设计 + 自动化实现

- **白盒**：interface、public、无 superinterface、`userRoot`/`systemRoot` 均为 abstract 且返回 `Preferences`。
- **黑盒**：匿名实现返回固定 in-memory `Preferences` 实例；断言 `userRoot()/systemRoot()` 精确返回注入实例、user/system 的 `isUserNode()` 语义、两树不同、返回节点可直接 `node("app")+put+get` 使用。

自动化实现：`testWhiteBox()` + `testBlackBox()`，共 14 断言。in-memory 节点，不写真实 prefs、不改全局工厂属性。
