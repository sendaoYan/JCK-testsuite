# java.awt.dnd.DropTargetContext 测试设计

## 1. API 功能
拖拽目标上下文，关联 DropTarget。

## 2. Java 用户如何使用
通过 DropTarget.getDropTargetContext() 获取；getDropTarget/getComponent。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/DropTargetContext.java

## 4. 测试用例设计 + 自动化实现
白盒: final；黑盒: 从 DropTarget 获取上下文、getDropTarget/getComponent。headful（需 DropTarget）。

自动化实现位于同目录 `DropTargetContextTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
