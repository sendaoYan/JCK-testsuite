# java.awt.print.PrinterJob 测试设计

## 1. API 功能
打印作业抽象类，提供打印对话框/属性。

## 2. Java 用户如何使用
getPrinterJob(); getCopies/setCopies/getUserName/setJobName/getJobName/isCancelled/cancel; printDialog 无头抛 HeadlessException。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/PrinterJob.java

## 4. 测试用例设计 + 自动化实现
白盒: 抽象；黑盒: getPrinterJob、copies/userName/jobName、cancel、printDialog 无头异常。

自动化实现位于同目录 `PrinterJobTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
