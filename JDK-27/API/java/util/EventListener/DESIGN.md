# java.util.EventListener — 测试设计案

## 1. API 功能
所有事件监听器接口的**标记超接口**：无任何方法/字段，仅作为类型层级根。

## 2. Java 用户如何使用
自定义监听器 `interface XListener extends EventListener {}`；事件源以 `EventListener` 类型
登记/分发（如 `addXListener(XListener)`）。
链路：定义子接口 -> 实现类 instanceof EventListener -> 源按 EventListener 分发。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/EventListener.java`（本地 `~/git/jdk25u-dev`）：
  “A tagging interface that all event listener interfaces must extend.”
- OpenJDK `src/java.desktop/share/classes/java/awt/event/ActionListener.java` extends
  EventListener——第 2 节反射验证。
- Spring 的 `ApplicationListener` 语义同源（事件监听器标记）。

## 4. 测试用例设计 + 自动化实现（EventListenerTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 可实现 | 实现类/子接口 instanceof EventListener |
| 领域超类型 | java.awt.event.ActionListener 可赋值给 EventListener（反射） |
| 反向 | String 不是 EventListener |

共 3 组场景、约 6 个 check（`testBlackBox()`）。

## 5. 契约深化（追加）：EventListenerProxy 包装契约

| 测试点 | 用例方法 |
|---|---|
| EventListenerProxy 抽象类、自身实现 EventListener（可作代理挂到监听列表） | testProxyContract |
| getListener() 返回构造参数同一实例（==）、稳定 | testProxyContract |
| 代理不触碰被包装监听器状态；equals 为同一性（两个包同一 listener 的代理不相等） | testProxyContract |
| 泛型边界 T extends EventListener 的指派方向（proxy→listener 可、反向不可） | testProxyContract |
