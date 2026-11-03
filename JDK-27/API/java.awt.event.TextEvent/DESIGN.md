# java.awt.event.TextEvent 行为测试设计

## 1. API 功能
文本变化事件：TEXT_VALUE_CHANGED=900，仅带源对象，无额外数据。

## 2. Java 用户如何使用
`new TextEvent(source, TEXT_VALUE_CHANGED)`；TextListener 读 getSource。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/TextEvent.java`；`TextComponent` 触发。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 AWTEvent、常量/构造器。黑盒：构造→回读 id/source；paramString TEXT_VALUE_CHANGED 与 unknown type；3 个常量精确值；id 范围关系。
对应 `TextEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
