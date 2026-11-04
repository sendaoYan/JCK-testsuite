# java.awt.AWTEvent 行为测试设计

## 1. API 功能
AWT 事件模型根类（抽象），继承 java.util.EventObject；提供事件 id、消费状态 consume/isConsumed、源对象 setSource/getSource、调试串 paramString/toString，以及 19 个事件掩码常量与 RESERVED_ID_MAX。

## 2. Java 用户如何使用
用户从不直接实例化 AWTEvent，而是用其子类（ActionEvent/KeyEvent/…）。事件由 EventQueue 分派，处理代码读取 getID()/getSource()，低层事件可 consume() 阻止默认处理；RESERVED_ID_MAX 供自定义事件类型选择 id。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/AWTEvent.java`（掩码常量、consume 白名单）；`EventQueue.java`（dispatchEvent 依 id 分派）；`test/jdk/java/awt/event/` 各事件测试。

## 4. 测试用例设计 + 自动化实现
白盒：抽象 public、继承 EventObject、protected id/consumed、掩码常量类型。黑盒：子类实例化→getID/getSource/paramString 空串/toString；consume 对低层 id 生效、语义 id 构造即 consumed；setSource 改源；掩码为 2 的幂且 RESERVED_ID_MAX==1999。
对应 `AWTEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
