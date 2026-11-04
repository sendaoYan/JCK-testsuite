# java.awt.dnd.DropTargetEvent 测试设计

## 1. API 功能
拖拽目标事件基类，携带上下文。

## 2. Java 用户如何使用
new DropTargetEvent(ctx)；getDropTargetContext。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/DropTargetEvent.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承 EventObject；黑盒: 构造 + getter。headful（需 DropTargetContext）。

自动化实现位于同目录 `DropTargetEventTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
