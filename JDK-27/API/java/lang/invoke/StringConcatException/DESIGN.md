# java.lang.invoke.StringConcatException — 设计方案

## 1. API 功能
字符串拼接链接失败异常（Exception 子类）：两个公有构造（message / message+cause），由 StringConcatFactory 在配方与签名失配等场景抛出。

## 2. Java 用户如何使用
```java
try { StringConcatFactory.makeConcatWithConstants(lk, "c", mt, "\u0001"); }
catch (StringConcatException e) { /* 链接失败 */ }
```

## 3. 开源代码实际用法调研
- JDK `StringConcatFactory.java`（argumentMismatch/invalidRecipe 抛出点）。
- javac 编译产物的链接期错误面：签名/配方不合法时用户首个可见异常即本类。

## 4. 测试用例设计 + 自动化实现
白盒：超类 Exception、2 个公有构造、serialVersionUID。
黑盒：message-only / message+cause 语义；toString 前缀 "java.lang.invoke.StringConcatException: msg"；suppressed 与 stackTrace 支持；真实触发（槽失配）拿到非空 message。
对应 `StringConcatExceptionTest.java` 的 `testBlackBox()`。