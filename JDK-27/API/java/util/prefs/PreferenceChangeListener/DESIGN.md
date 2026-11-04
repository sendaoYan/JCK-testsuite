# java.util.prefs.PreferenceChangeListener 测试方案

白盒结构断言在 `PreferenceChangeListenerTest.testWhiteBox()`，黑盒行为断言在 `PreferenceChangeListenerTest.testBlackBox()`。

## 1. API 功能

- 接口方法：`void preferenceChange(PreferenceChangeEvent evt)`。
- 继承自 `java.util.EventListener`；单一抽象方法，可用 lambda 实现；接收键值变更的异步通知。

## 2. Java 用户如何使用

- lambda/匿名类实现，注册到 `Preferences.addPreferenceChangeListener`。
- `put(key, value)` 后投递 `preferenceChange`，回调中 `evt.getNode()/getKey()/getNewValue()` 取变更详情。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`。

- `src/utils/IdealGraphVisualizer/NetworkConnection/src/main/java/com/sun/hotspot/igv/connection/Server.java:54`：`Settings.get().addPreferenceChangeListener(this)` 注册监听。
- `src/java.prefs/share/classes/java/util/prefs/AbstractPreferences.java`：`put`/`remove` 内部调用 `enqueuePreferenceChangeEvent` 投递该接口事件。

## 4. 测试用例设计 + 自动化实现

- **白盒**：interface、public、`extends EventListener`、`preferenceChange` abstract、仅一个声明方法（函数式接口）。
- **黑盒**：in-memory `AbstractPreferences`，注册 lambda 监听后 `put("key","value")` 投递事件；断言 `getNode/getKey/getNewValue` 载荷；`removePreferenceChangeListener` 可移除；用 `CountDownLatch` 等待异步投递。

自动化实现：`testWhiteBox()` + `testBlackBox()`，共 10 断言。
