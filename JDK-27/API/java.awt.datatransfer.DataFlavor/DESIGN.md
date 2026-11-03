# java.awt.datatransfer.DataFlavor 测试设计

## 1. API 功能
数据 MIME 类型描述，含常量 stringFlavor/plainTextFlavor/javaFileListFlavor 等。

## 2. Java 用户如何使用
多个构造（mime / mime+humanName / Class+mime / mime+ClassLoader）；getMimeType/getPrimaryType/getSubType/getParameter/match/isMimeTypeEqual/isFlavorTextType/...；clone/equals/hashCode。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/datatransfer/DataFlavor.java

## 4. 测试用例设计 + 自动化实现
白盒: Externalizable+Cloneable；黑盒: 常量、构造、MIME 分解、humanName、match、clone、equals。

自动化实现位于同目录 `DataFlavorTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
