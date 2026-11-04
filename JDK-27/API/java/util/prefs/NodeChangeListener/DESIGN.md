# java.util.prefs.NodeChangeListener 测试方案

白盒结构断言在 `NodeChangeListenerTest.testWhiteBox()`，黑盒行为断言在 `NodeChangeListenerTest.testBlackBox()`。

## 1. API 功能

- 接口方法：`void childAdded(NodeChangeEvent evt)`、`void childRemoved(NodeChangeEvent evt)`。
- 继承自 `java.util.EventListener`；用于接收首选项子节点新增/删除的异步通知。

## 2. Java 用户如何使用

- 匿名类或 lambda 实现，注册到 `Preferences.addNodeChangeListener`。
- 当调用 `node("child")` 创建新节点时投递 `childAdded`；`removeNode()` 时投递 `childRemoved`；回调中通过 `evt.getParent()/getChild()` 取节点。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`。

- `test/jdk/java/util/prefs/AddNodeChangeListener.java:50`：`userRoot.addNodeChangeListener(ncla)` 注册节点监听。
- `test/jdk/java/util/prefs/AddNodeChangeListener.java:127-132`：匿名 `NodeChangeListener` 实现 `childAdded`/`childRemoved` 并校验载荷。

## 4. 测试用例设计 + 自动化实现

- **白盒**：interface、public、`extends EventListener`、`childAdded`/`childRemoved` 均为 abstract。
- **黑盒**：in-memory `AbstractPreferences`（`childSpi` 置 `newNode=true` 触发新增事件），注册监听后 `node("child")` 投递 `childAdded`（断言 parent/child 载荷）、`removeNode()` 投递 `childRemoved`；用 `CountDownLatch` 等待异步投递。

自动化实现：`testWhiteBox()` + `testBlackBox()`，共 12 断言。
