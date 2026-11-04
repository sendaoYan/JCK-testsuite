# java.awt.dnd.DragGestureRecognizer 测试设计

## 1. API 功能
拖拽手势识别器抽象基类。

## 2. Java 用户如何使用
匿名实现 registerListeners/unregisterListeners；get/setComponent、get/setSourceActions、add/remove DragGestureListener、resetRecognizer。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/DragGestureRecognizer.java

## 4. 测试用例设计 + 自动化实现
白盒: 抽象、Serializable；黑盒: 匿名实现、动作/组件/监听器。headful（需 DragSource）。

自动化实现位于同目录 `DragGestureRecognizerTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
