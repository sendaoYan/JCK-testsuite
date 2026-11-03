# java.awt.PrintJob 测试设计

## 1. API 功能
抽象打印作业，定义获取图形/页面尺寸/分辨率/结束等。

## 2. Java 用户如何使用
匿名实现 5 个抽象方法。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/PrintJob.java

## 4. 测试用例设计 + 自动化实现
白盒: 抽象；黑盒: 匿名实现返回值。

自动化实现位于同目录 `PrintJobTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
