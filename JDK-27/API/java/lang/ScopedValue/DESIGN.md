# ScopedValue — 设计方案

## 1. API 功能
`java.lang.ScopedValue<T>`（final，JDK 21 引入）是作用域值，替代 `ThreadLocal` 用于结构化并发：
- 工厂：`newInstance()`
- 绑定：静态 `where(ScopedValue, value)` 返回 `Carrier`；`Carrier.where` 链式
- 读取：`get/isBound/orElse/orElseThrow`；`Carrier.run/call`

## 2. Java 用户如何使用
- 在 `Carrier.run/call` 作用域内绑定值，作用域结束自动解除。
```java
ScopedValue<String> ctx = ScopedValue.newInstance();
ScopedValue.where(ctx, "req-1").run(() -> System.out.println(ctx.get()));
```

## 3. 开源代码实际用法调研
- JDK 结构化并发（`StructuredTaskScope`）用 `ScopedValue` 传播上下文。
- JDK `test/jdk/java/lang/ScopedValue` 系列验证绑定/解绑/重绑定/继承。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ScopedValue")` 验证 final、非接口。
黑盒：未绑定 `isBound=false`、`orElse` 默认值、`get` 抛 `NoSuchElementException`、`orElseThrow` 抛供应商异常；绑定 `Carrier.get/call/run`、`where` 覆盖、原始 carrier 不变、作用域外解绑；链式 `where` 多值。
对应 `<Simple>Test.java` 黑盒部分。
