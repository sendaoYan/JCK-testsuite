# java.awt.print.Paper 测试设计

## 1. API 功能
纸张尺寸与可成像区域。

## 2. Java 用户如何使用
new Paper(); getWidth/getHeight/setSize/setImageableArea/getImageable*/clone。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/Paper.java

## 4. 测试用例设计 + 自动化实现
白盒: Cloneable；黑盒: 默认 LETTER、setSize/setImageableArea、clone。

自动化实现位于同目录 `PaperTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
