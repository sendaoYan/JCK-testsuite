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

## 契约深化（2026-11，批次 lcL）

### 1. API 功能（本轮新增维度）
- create() 幂等独立：两次 create() 得到不同 Cleaner（身份 equals、hashCode 稳定、toString 形如 java.lang.ref.Cleaner@<hex>），各自可注册。
- **ThreadFactory 在 create() 时恰被调用一次**（jdk-27.0.1 实测），register()/clean() 不再增线程；工厂 daemon 标志被尊重。
- Cleanable 接口面：public 接口、恰一个方法 `void clean()`（public abstract）；register 返回值 instanceof Cleanable 且 **instanceof Reference**（实测实现为 jdk.internal.ref.CleanerImpl$PhantomCleanableRef——phantom 基实现，仅记录实现面）。
- 手动 clean() 与可达性无关：referent 强引用在场仍立即执行动作；同一对象可重复注册+清理。
- 批量独立性（30 注册）：Cleanable 两两不同、清理前零执行、清一半恰执行一半、全清后每个恰一次、总和恰 30。
- 跨 Cleaner 独立：同一对象在两个 Cleaner 各自注册，清理互不影响。
- 跨线程 clean()：另一 daemon 线程调 clean() 两次 → 动作恰一次（确定性，无 GC 依赖）；worker 线程 try/catch(Throwable) 内吞（jtreg MainThreadGroup 约束）。
- 自动清理尽力（≤10 gc×≤10ms latch）：对象不可达后动作自动执行恰一次且运行在 daemon 清理线程；超时 println 跳过。
- 手动 clean 后的自动清理**不再执行**（计数不增，尽力验证超时则跳过）。
- 抛异常动作：异常从手动 clean() 传播（UnsupportedOperationException 实测），首次失败后注册即注销——第二次 clean() 静默无操作且**不重试**（at-most-once）。

### 2. 使用方式
- 抛异常动作的用例必须保 referent 强可达（避免自动清理在清理线程触发异常路径——jtreg 线程组会判负）。

### 3. 参考来源
- Oracle javadoc java.lang.ref.Cleaner/Cleanable（register 的 NPE、at-most-once、"cleaning action runs at most once"；Cleanable "Cleanable represents an object and a cleaning action registered in a Cleaner"）。
- jdk-27.0.1 实测：工厂调用次数、PhantomCleanableRef 类型、daemon 线程、异常传播与注销。

### 4. 测试用例设计 + 易错规范细节
- testCreateSemantics（9）、testFactoryThreadCreation（5）、testCleanableSurface（6）、testManualCleanWithStrongReference（3）、testBulkIndependence（30+5）、testCrossCleanerIndependence（6）、testCleanFromAnotherThread（3）、testAutoCleanBestEffort（0-2 尽力）、testAutoCleanAfterManualCleanBestEffort（1+尽力）、testCleanableUsableAfterClean（4）、testStronglyHeldThrowingAction（4）。
- 易错点：① 自动清理断言全部走 latch 有界等待，绝不无限 join；② 清理动作绝不能抛（自动路径异常会打到清理线程）；③ 二次 clean() 对失败注册也是静默 no-op（勿断言二次传播）。
