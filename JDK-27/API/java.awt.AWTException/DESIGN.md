# java.awt.AWTException 测试设计

## 1. API 功能
异常类，继承 java.lang.Exception，携带一条消息字符串。

## 2. Java 用户如何使用
new AWTException(msg) 构造，getMessage() 取回消息，toString() 可打印，instanceof java.lang.Exception 成立。

## 3. 开源代码实际用法调研
JDK 源码: ~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/AWTException.java

## 4. 测试用例设计 + 自动化实现
白盒: 继承 java.lang.Exception、声明 getMessage；黑盒: 构造实例、getMessage 非空、equals/toString 可调用、instanceof 成立。

自动化实现位于同目录 `AWTExceptionTest.java`，白盒结构断言放在 `testWhiteBox()`，黑盒组合操作断言放在 `testBlackBox()`，由 `main` 依次调用并打印 `PASS (N assertions)`。
