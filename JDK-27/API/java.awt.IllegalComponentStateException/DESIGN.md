# java.awt.IllegalComponentStateException 测试设计

## 1. API 功能
异常类，继承 java.lang.IllegalStateException，支持无参与带消息两种构造。

## 2. Java 用户如何使用
new IllegalComponentStateException() 或 new IllegalComponentStateException(msg)，getMessage() 取消息。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/IllegalComponentStateException.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承关系；黑盒: 两种构造、getMessage、instanceof。

自动化实现位于同目录 `IllegalComponentStateExceptionTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
