# java.util.prefs.NodeChangeEvent 测试方案

白盒结构断言在 `NodeChangeEventTest.testWhiteBox()`，黑盒行为断言在 `NodeChangeEventTest.testBlackBox()`。

## 1. API 功能

- 构造器：`NodeChangeEvent(Preferences parent, Preferences child)`。
- 查询：`getParent()`（返回父节点）、`getChild()`（返回新增/被删除的子节点）；继承自 `java.util.EventObject`（`getSource()` 为 parent）。

## 2. Java 用户如何使用

- 由首选项实现内部在节点新增/删除时创建并投递给 `NodeChangeListener.childAdded/childRemoved`。
- 用户（或测试）构造事件以校验载荷：`new NodeChangeEvent(parent, child)` 后断言 `getParent()==parent`、`getChild()==child`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`。

- `src/java.prefs/share/classes/java/util/prefs/AbstractPreferences.java`：内部 `NodeAddedEvent`/`NodeRemovedEvent` 继承 `NodeChangeEvent` 并在 `node()`/`removeNode()` 时投递。
- `test/jdk/java/util/prefs/AddNodeChangeListener.java:127-132`：`childAdded(NodeChangeEvent evt)`/`childRemoved(NodeChangeEvent evt)` 回调接收该事件。

## 4. 测试用例设计 + 自动化实现

- **白盒**：public 具体类、`extends EventObject`、public `(Preferences, Preferences)` 构造器、`getParent`/`getChild` 存在。
- **黑盒**：构造后 `getParent()/getChild()` 精确相等、`getSource()==parent`、`instanceof EventObject`；不同节点构造的事件载荷互不干扰。

自动化实现：`testWhiteBox()` + `testBlackBox()`，共 10 断言。in-memory `AbstractPreferences` 提供节点，不写真实 prefs。
