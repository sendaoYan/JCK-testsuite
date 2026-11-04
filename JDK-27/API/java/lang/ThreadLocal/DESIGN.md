# ThreadLocal — 设计方案

## 1. API 功能
`java.lang.ThreadLocal<T>` 提供线程局部变量：
- `get()`：返回当前线程的值；未设则调用 `initialValue()` 初始化
- `set(T)`/`remove()`
- 工厂：`withInitial(Supplier)`；子类可重写 `protected initialValue()`

## 2. Java 用户如何使用
- 每个线程独立存储：`tl.set(v)` 后同线程 `get()` 取回，其他线程看不到。
- 常见：`SimpleDateFormat`/数据库连接按线程缓存，避免线程安全开销。
```java
private static final ThreadLocal<DateFormat> DF =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
DateFormat f = DF.get();   // 当前线程专属实例
```

## 3. 开源代码实际用法调研
- Spring `RequestContextHolder`/`TransactionSynchronizationManager` 用 `ThreadLocal` 绑定当前请求/事务。
- Log4j2 `ThreadContext`、JDBC 连接绑定均依赖 `ThreadLocal`。
- JDK `test/jdk/java/lang/ThreadLocal` 验证线程隔离与 `withInitial`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ThreadLocal")` 验证非接口、父类 `Object`、`get/set/remove` 存在。
黑盒：默认初始值 null；`set/get/remove` 往返；`initialValue` 重写；`withInitial` 初始化 + remove 后再初始化；跨线程隔离（子线程看不到主线程值），子线程 daemon + join。
对应 `<Simple>Test.java` 黑盒部分。
