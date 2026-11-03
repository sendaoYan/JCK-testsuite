# java.awt.dnd.DragSourceContext 测试设计

## 1. API 功能
拖拽源上下文，跟踪拖拽状态与游标/可传输数据。

## 2. Java 用户如何使用
new DragSourceContext(trigger, cursor, image, offset, transferable, listener)；getDragSource/getComponent/getTrigger/getSourceActions/getTransferable/getCursor/setCursor。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/dnd/DragSourceContext.java

## 4. 测试用例设计 + 自动化实现
白盒: 实现 DragSourceListener+MotionListener；黑盒: 构造 + getter + 游标/监听器。headful。

自动化实现位于同目录 `DragSourceContextTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
