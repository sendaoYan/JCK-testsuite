# java.awt.print.PrinterIOException 测试设计

## 1. API 功能
打印 IO 异常，包装 IOException。

## 2. Java 用户如何使用
new PrinterIOException(IOException)；getIOException/getCause。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/PrinterIOException.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承 PrinterException、声明 getIOException；黑盒: getIOException/getCause 同一实例。

自动化实现位于同目录 `PrinterIOExceptionTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
