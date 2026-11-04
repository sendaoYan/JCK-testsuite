# java.util.prefs.PreferenceChangeEvent 测试方案

白盒结构断言在 `PreferenceChangeEventTest.testWhiteBox()`，黑盒行为断言在 `PreferenceChangeEventTest.testBlackBox()`。

## 1. API 功能

- 构造器：`PreferenceChangeEvent(Preferences node, String key, String newValue)`。
- 查询：`getNode()`（发生变更的首选项节点）、`getKey()`（键）、`getNewValue()`（新值，删除时为 `null`）；继承自 `java.util.EventObject`（`getSource()` 为 node）。

## 2. Java 用户如何使用

- 由实现内部在 `put`/`remove` 时创建并投递给 `PreferenceChangeListener.preferenceChange`。
- 用户（或测试）构造以校验载荷：`new PreferenceChangeEvent(node, "key", "newValue")` 后断言 `getNode/getKey/getNewValue`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`。

- `src/java.prefs/share/classes/java/util/prefs/AbstractPreferences.java`：`enqueuePreferenceChangeEvent(key, newValue)` 在 `put`/`remove`（remove 时 `newValue==null`）构造并投递该事件。
- `src/utils/IdealGraphVisualizer/NetworkConnection/src/main/java/com/sun/hotspot/igv/connection/Server.java:54`：`addPreferenceChangeListener(this)` 消费此类事件。

## 4. 测试用例设计 + 自动化实现

- **白盒**：public 具体类、`extends EventObject`、public `(Preferences, String, String)` 构造器、`getNode/getKey/getNewValue` 存在。
- **黑盒**：构造后 `getNode()/getKey()/getNewValue()` 精确相等、`getSource()==node`；删除通知允许 `newValue==null` 且 `getKey()` 正确。

自动化实现：`testWhiteBox()` + `testBlackBox()`，共 12 断言。in-memory 节点，不写真实 prefs。
