# java.awt.dnd.DragGestureListener 测试设计

## 1. API 功能
拖拽手势监听接口。

## 2. Java 用户如何使用
匿名实现 dragGestureRecognized。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/DragGestureListener.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口、继承 EventListener；黑盒: dragGestureRecognized 可调用。

自动化实现位于同目录 `DragGestureListenerTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
