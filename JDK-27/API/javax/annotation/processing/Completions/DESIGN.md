# javax.annotation.processing.Completions — 设计方案

## 1. API 功能
工厂：构造 `Completion`（取值 + 消息）。

## 2. Java 用户如何使用
`Completions.of(value)` / `of(value, message)`。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/annotation/processing/Completions.java`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言工厂方法；黑盒断言 of(value) 空消息与 of(value,msg)。 对应 `CompletionsTest.java` 的 `testBlackBox()`。
