# java.awt.print.PageFormat 测试设计

## 1. API 功能
页面格式，描述纸张大小、方向、可成像区域。

## 2. Java 用户如何使用
new PageFormat(); getWidth/getHeight/getImageable*/getOrientation/setOrientation/getPaper/setPaper/getMatrix/clone。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/PageFormat.java

## 4. 测试用例设计 + 自动化实现
白盒: Cloneable；黑盒: 默认 LETTER、方向往返、非法方向 IAE、getMatrix、clone。

自动化实现位于同目录 `PageFormatTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
