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

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **生命周期全表**：set 覆盖旧值、remove 幂等（二次 remove 后 get 触发 initialValue）、set(null) 合法
  且不等于"未设置"（不重触发 initialValue）。
- **withInitial 契约**：supplier 每线程至多一次；**remove 后重新武装**（再次 get 调 supplier 得新实例）；
  withInitial(null)→NPE；supplier 抛异常原样传播（消息保留）。
- **身份语义**：同一 ThreadLocal 在不同线程返回各自实例（两个 latch 同步 worker 互不串扰）；
  同线程多个 ThreadLocal 独立；值可变时同线程内共享同一实例（Box 字段可见）。
- **initialValue 子类路径**：覆盖 initialValue 与 withInitial 各自独立计数
  （主线程第 1 次调用、worker 第 2 次——共享计数器确定性编号）。

### 使用方式
get→（惰性 initialValue/withInitial supplier）→set/remove 重置；跨线程靠 ThreadLocal 实例本身隔离。

### 参考来源
Oracle javadoc（remove 后 initialValue 可再触发）；双侧实测行为一致（13→122 断言）。

### 测试用例设计 + 易错规范细节
- 新增 testLifecycleFull/testWithInitialContract/testWorkerIsolationLatched/testInitialValuePerThread/
  testSequentialWorkersIsolated/testManyLocals 等 13 个方法。
- 易错点：**remove 会重新武装 supplier**；**set(null) 与 remove 语义不同**；
  worker 的 initialValue 调用次序要用共享计数器显式编号（不可依赖线程启动顺序）。
