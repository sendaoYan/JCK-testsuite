# java.awt.event.ComponentEvent 行为测试设计

## 1. API 功能
组件变化事件的基类（移动/缩放/显示/隐藏），提供 getComponent() 返回源组件，常量 COMPONENT_MOVED=100..COMPONENT_HIDDEN=103。

## 2. Java 用户如何使用
`new ComponentEvent(component, COMPONENT_RESIZED)`；ComponentListener 回调中读 getComponent。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/ComponentEvent.java`（paramString 带 bounds）；`test/jdk/java/awt/event/ComponentEvent/`。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 AWTEvent、常量与构造器。黑盒：构造→getID/getSource/getComponent 回读；4 个常量精确值；paramString 对 moved/resized 带 bounds、hidden/shown 纯文本、未知 id 为 unknown type。
对应 `ComponentEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- COMPONENT_MOVED/RESIZED/SHOWN/HIDDEN 与 FIRST/LAST(100..103) 精确值。
- 消费语义：COMPONENT_* id 不可消费，consume() 为 no-op（isConsumed 恒 false）——锁定参考实现行为。
- paramString 精确格式（`COMPONENT_MOVED (0,0 0x0)`、`unknown type`）。
- 序列化：id 保留；EventObject.source 为 transient，getComponent() round-trip 后为 null。
