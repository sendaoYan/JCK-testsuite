# ScopedValue.CallableOp — 设计方案

## 1. API 功能
`java.lang.ScopedValue.CallableOp<T, X extends Throwable>`（接口）是 `ScopedValue.Carrier.call` 的回调类型：唯一抽象方法 `T call() throws X`，允许向调用者传播受检异常（不同于 `java.util.concurrent.Callable`）。

## 2. Java 用户如何使用
- 作为 `Carrier.call(CallableOp)` 的 lambda/方法引用，在绑定作用域内执行会抛受检异常的计算：
```java
String v = ScopedValue.where(KEY, "x").call(() -> mightThrow());
```

## 3. 开源代码实际用法调研
- JEP 506（ScopedValue 正式）设计文档与 JDK `test/jdk/java/lang/ScopedValue` 中 `call(...)` 的受检异常用例；虚拟线程上下文传递（Jetty/Loom 原型）是主要消费场景。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ScopedValue$CallableOp")` 验证接口、双类型参数 `<T, X>`、`call()` public abstract 且泛型异常子句为 X。
黑盒：lambda 返回值、受检异常（IOException）经 `call()` 原样传播（类型+message）、方法引用形态作为 `Carrier.call` 回调在作用域内取值。
对应 `CallableOpTest.java` 白盒 + 黑盒部分。
