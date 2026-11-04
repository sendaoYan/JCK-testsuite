# InheritableThreadLocal — 设计方案

## 1. API 功能
`java.lang.InheritableThreadLocal<T>` 扩展 `ThreadLocal<T>`：
- 子线程在创建时继承父线程的值（快照）
- 子类可重写 `protected childValue(T parentValue)` 变换继承值

## 2. Java 用户如何使用
- 让请求上下文/事务标识等自动传播到子线程（如异步任务继承当前用户会话）。
```java
InheritableThreadLocal<String> ctx = new InheritableThreadLocal<>();
ctx.set("user-1");
new Thread(() -> System.out.println(ctx.get())).start(); // "user-1"
```

## 3. 开源代码实际用法调研
- Spring `RequestContextHolder` 曾有 `inheritable` 变体以向子线程传播请求属性。
- 框架在任务提交到线程池前通过 `InheritableThreadLocal` 捕获上下文并传播（JDK `test/jdk/java/lang/InheritableThreadLocal` 验证继承语义）。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证父类为 `ThreadLocal`、非接口。
黑盒：子线程继承父线程值（创建时快照）；`childValue` 变换继承值；`instanceof ThreadLocal`。子线程 daemon + join。
对应 `<Simple>Test.java` 黑盒部分。
