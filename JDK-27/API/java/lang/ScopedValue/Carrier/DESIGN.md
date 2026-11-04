# ScopedValue.Carrier — 设计方案

## 1. API 功能
`java.lang.ScopedValue.Carrier`（final，无公开构造器）累积 `ScopedValue → 值` 绑定并划定作用域：`where(key, value)` 链式叠加（同键后写覆盖仅影响派生 Carrier）、`get(key)` 预览绑定、`call(CallableOp)`/`run(Runnable)` 在作用域内执行并在退出时自动解绑（可传播受检异常）。

## 2. Java 用户如何使用
- 一次绑定多个上下文值并在受限范围内可见（替代 ThreadLocal 传参）：
```java
String r = ScopedValue.where(USER, "alice").where(TRACE, true).call(() -> service());
```

## 3. 开源代码实际用法调研
- JEP 506 与 JDK `test/jdk/java/lang/ScopedValue/Basic.java` 覆盖多绑定、嵌套覆盖、异常后退绑、作用域外 `get()` 抛 `NoSuchElementException`；虚拟线程框架（Helidon Níma 风格）以 Carrier 传请求上下文。

## 4. 测试用例设计 + 自动化实现
白盒：验证 final、无公开构造器、`where/get/call/run` 公开。
黑盒：单/双绑定 `get`；`where` 同键覆盖只影响派生 Carrier；`call/run` 内可见、出域即解绑（`isBound` false、`get()` 抛 NSEE）；嵌套作用域覆盖与外层恢复；`call` 传播受检 IOException 且作用域回退。
对应 `CarrierTest.java` 白盒 + 黑盒部分。
