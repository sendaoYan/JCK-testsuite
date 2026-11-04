# java.lang.ref.Cleaner — 设计方案

## 1. API 功能
final 工具类（无 public 构造器）：对象变为 phantom-reachable 后运行清理动作（替代 finalize）：
- 工厂：`static create()`、`static create(ThreadFactory)`（均非 null 校验）
- 注册：`Cleanable register(Object obj, Runnable action)` —— 返回 `Cleaner.Cleanable`，显式 `clean()` 立即执行动作（不依赖 GC）

## 2. Java 用户如何使用
try-with-resources 风格的显式清理 + 兜底自动清理：
```java
class Resource implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    Resource() { cleanable = CLEANER.register(this, () -> freeNative()); }
    @Override public void close() { cleanable.clean(); }  // 幂等
}
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/ref/Cleaner.java`：`register` 对 obj/action 做 `Objects.requireNonNull`；实现委托 `jdk.internal.ref.CleanerImpl`（daemon 线程消费 ReferenceQueue）。
- JDK `src/java.base/share/classes/jdk/internal/ref/PhantomCleanable.java`：`clean()` 以链表移除做 at-most-once 守卫，再 `performCleanup()`；异常从 `clean()` 传播。
- JDK `test/jdk/java/lang/ref/CleanerTest.java`：`TestCleanup` 覆盖 register/多对象/多 Cleaner/异常动作等场景。
- Netty `io.netty.util.internal.ObjectCleaner` 提供同类 register+clean 语义。

## 4. 测试用例设计 + 自动化实现
白盒：final 类、无 public 构造器、`create`/`create(ThreadFactory)` static、`register` 返回 `Cleanable` 且无 checked 异常。
黑盒（全部显式 clean，不依赖 GC）：
- register→clean 动作恰好执行一次，重复 clean 幂等（at-most-once）；
- 同一对象两个注册相互独立；
- `register(null, …)`/`register(…, null)` 抛 NPE；`create(null)` 抛 NPE；
- `create(ThreadFactory)` 用 daemon 工厂创建的 Cleaner 正常 register/clean；
- 动作抛异常时从 `clean()` 传播，且不重试。
对应 `CleanerTest.java` 的 `testBlackBox()`。
