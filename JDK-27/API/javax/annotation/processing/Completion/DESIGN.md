# javax.annotation.processing.Completion — 设计方案

## 1. API 功能
接口：补全项（getValue/getMessage）。

## 2. Java 用户如何使用
匿名实现或 Completions.of 构造。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/annotation/processing/Completion.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言接口方法；黑盒匿名实现断言 getValue/getMessage。 对应 `CompletionTest.java` 的 `testBlackBox()`。
