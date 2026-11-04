# java.awt.dnd.DragSource 测试设计

## 1. API 功能
拖拽源，管理拖拽手势识别与监听器。

## 2. Java 用户如何使用
new DragSource(); getFlavorMap/getDefaultDragSource/getDragThreshold/isDragImageSupported/add/remove DragSourceListener 与 MotionListener。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/DragSource.java

## 4. 测试用例设计 + 自动化实现
白盒: Serializable；黑盒: 构造、flavorMap、默认源、阈值、监听器增删。headful（构造抛 HeadlessException）。

自动化实现位于同目录 `DragSourceTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
