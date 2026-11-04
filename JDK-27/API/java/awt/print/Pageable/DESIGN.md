# java.awt.print.Pageable 测试设计

## 1. API 功能
可打印文档接口，含 UNKNOWN_NUMBER_OF_PAGES 常量。

## 2. Java 用户如何使用
匿名实现 getNumberOfPages/getPageFormat/getPrintable。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/Pageable.java

## 4. 测试用例设计 + 自动化实现
白盒: 接口；黑盒: 常量与匿名实现。

自动化实现位于同目录 `PageableTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
