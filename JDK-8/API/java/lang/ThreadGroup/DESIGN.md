# java.lang.ThreadGroup 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「读名字/父链」的冒烟。重点补：白盒结构（implements
> `Thread.UncaughtExceptionHandler`、两个构造器）、构造器 null NPE、父子链、max/setMaxPriority 继承、
> daemon、activeCount/enumerate 线程与线程组、destroy 空组成功 vs 非空组 ITSE、interrupt/uncaughtException/
> list/toString/checkAccess/allowThreadSuspension 可调用。断言确定，worker 全 daemon + 有界 join，不留活线程。

## 1. API 提供了哪些功能

- **类**：`java.lang.ThreadGroup`（implements `Thread.UncaughtExceptionHandler`），两个构造器
  `(String name)` 与 `(ThreadGroup parent, String name)`。
- **命名/层级**：`getName`、`getParent`。
- **优先级**：`getMaxPriority`/`setMaxPriority`（子组继承父组 max，setMaxPriority 只降不升子线程已有效率）。
- **daemon**：`isDaemon`/`setDaemon`。
- **生命周期**：`isDestroyed`/`destroy`（空组可毁，非空组抛 `IllegalThreadStateException`）。
- **枚举**：`activeCount`/`activeGroupCount`、`enumerate(Thread[],boolean)`/`enumerate(Thread[])`/
  `enumerate(ThreadGroup[],boolean)`/`enumerate(ThreadGroup[])`。
- **控制**：`interrupt`、`list`、`checkAccess`、`toString`、`uncaughtException`、
  已弃用的 `suspend`/`resume`/`stop`/`allowThreadSuspension`。

## 2. Java 用户如何使用

- `new ThreadGroup("pool")` 建组后 `new Thread(group, task)` 把线程归组；`group.enumerate(...)` 列出线程。
- 未捕获异常默认从线程转发到其 ThreadGroup 的 `uncaughtException`，再向上转发父组。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/ThreadGroup.html`）：构造器 null NPE、destroy ITSE、
  setMaxPriority、enumerate、uncaughtException 转发。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/ThreadGroup.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：子组继承父组 max priority、destroy 空/非空行为。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/ThreadGroup`。

## 4. 测试用例设计（映射到 ThreadGroupTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：非接口、实现 UncaughtExceptionHandler、两构造器 | testWhiteBox |
| P0 | 构造器：名字/父链、null 父 NPE、null 名存 null | testConstructors |
| P0 | max priority：子组继承父组、setMaxPriority 生效 | testPriority |
| P0 | daemon：默认 false、setDaemon(true) | testDaemon |
| P0 | activeCount/enumerate 线程/线程组（daemon worker + 有界 join） | testCountsAndEnumerate |
| P0 | destroy：空组成功且 isDestroyed、非空组 ITSE | testDestroy |
| P1 | toString/checkAccess/allowThreadSuspension/uncaughtException/interrupt/list 可调用 | testMisc |

## 关键规范细节（易错，已锁入断言）

- **构造器 null 父抛 `NullPointerException`；null 名不抛**（JDK-8 直接把 null 名存下来，
  `getName()` 返回 null）；`ThreadGroup(String)` 父为当前线程组。
- **子组 `getMaxPriority()` 继承父组当前 max**；`setMaxPriority` 只对组生效（不影响已建线程）。
- **`destroy()` 空组成功、非空组抛 `IllegalThreadStateException`**；销毁后 `isDestroyed()` 为 true。
- **worker 线程全部 `setDaemon(true)`**，用 `CountDownLatch` 保活 + `join(10s)` 有界回收，不留非 daemon 线程。
- **`uncaughtException`/`interrupt`/`list`/`checkAccess`/`allowThreadSuspension` 空组直接调用不抛**。

## 注意事项

- 不调用已弃用的 `suspend`/`resume`/`stop`（有死锁/安全风险）；`allowThreadSuspension` 仅 no-op 调用。
- headless 安全，不加 `@key`；不依赖 sleep 时序，全部用 CountDownLatch + 有界超时。
