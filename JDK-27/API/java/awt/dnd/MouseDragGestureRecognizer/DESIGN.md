# java.awt.dnd.MouseDragGestureRecognizer 测试设计

## 1. API 功能
鼠标拖拽手势识别器抽象基类。

## 2. Java 用户如何使用
匿名实现；实现 MouseListener+MouseMotionListener；getDragSource/getSourceActions。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/MouseDragGestureRecognizer.java

## 4. 测试用例设计 + 自动化实现
白盒: 抽象、继承 DragGestureRecognizer、实现鼠标监听；黑盒: 匿名实现、鼠标方法可调用。headful（需 DragSource）。

自动化实现位于同目录 `MouseDragGestureRecognizerTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
