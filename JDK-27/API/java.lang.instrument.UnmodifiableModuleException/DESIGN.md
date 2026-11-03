# UnmodifiableModuleException — 设计方案

## 1. API 功能
`java.lang.instrument.UnmodifiableModuleException`（继承 `RuntimeException`）表示 `redefineModule` 尝试修改不可修改的模块。构造器 `()` 与 `(String message)`。

## 2. Java 用户如何使用
```java
try { inst.redefineModule(m, ...); }
catch (UnmodifiableModuleException e) { /* 该模块不可修改 */ }
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/instrument` 用例断言对 `java.base` 等不可修改模块 redefineModule 抛该异常。
- agent 框架对不可修改模块捕获该运行时异常。

## 4. 测试用例设计 + 自动化实现
白盒：具体类 + 继承 RuntimeException + 两个构造器。
黑盒：no-arg message null；String 构造 message/cause；instanceof RuntimeException。
对应 `UnmodifiableModuleExceptionTest.java` 白盒 + 黑盒部分。
