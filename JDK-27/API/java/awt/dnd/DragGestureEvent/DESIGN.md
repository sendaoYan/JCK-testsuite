# java.awt.dnd.DragGestureEvent 测试设计

## 1. API 功能
拖拽手势事件，携带识别器、动作、起点、触发事件序列。

## 2. Java 用户如何使用
new DragGestureEvent(recognizer, action, origin, events)；getDragAction/getDragOrigin/getComponent/getDragSource/getSourceAsDragGestureRecognizer/iterator/toArray。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/DragGestureEvent.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承 EventObject；黑盒: 构造 + getter。headful（需 DragSource+Component）。

自动化实现位于同目录 `DragGestureEventTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
