# java.awt.dnd.DragSourceDragEvent 测试设计

## 1. API 功能
拖拽源拖动事件，携带目标动作、丢弃动作、手势修饰键。

## 2. Java 用户如何使用
new DragSourceDragEvent(ctx, targetActions, dropAction, gestureModifiers)；getTargetActions/getDropAction/getUserAction/getGestureModifiers。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/DragSourceDragEvent.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承 DragSourceEvent；黑盒: 构造 + getter。headful。

自动化实现位于同目录 `DragSourceDragEventTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
