# java.lang.Thread 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「构造一个线程跑一下」的冒烟。重点补：构造器矩阵（null name→NPE、
> null group→父 group、null Runnable 合法、负 stackSize 接受）、daemon 标志继承/启动后设置→ITSE、
> 双重 start→ITSE、setPriority 越界→IAE、join/sleep/interrupt 标志语义、holdsLock、
> uncaught handler（实例默认非 null、全局默认 null）、ThreadGroup 构造器、4 参构造器、
> clone() 恒抛 CloneNotSupportedException、白盒结构（final 与否/实现 Runnable/native/static）。
> 断言不依赖墙钟随机/Locale/时区，headless 安全；线程全部 daemon 或 join 有界超时，不留非 daemon 线程。

## 1. API 提供了哪些功能

- **构造器（8 个）**：`Thread()`、`Thread(Runnable)`、`Thread(Runnable,String)`、`Thread(String)`、
  `Thread(ThreadGroup,Runnable)`、`Thread(ThreadGroup,Runnable,String)`、`Thread(ThreadGroup,String)`、
  `Thread(ThreadGroup,Runnable,String,long)`（4 参带 stackSize）。JDK-8 无 protected 5 参构造器（Java 21+）。
- **字段/状态**：`getName/setName`、`getId`、`getPriority/setPriority`、`isDaemon/setDaemon`、
  `isAlive`、`getState`、`getThreadGroup`、`isInterrupted`。
- **生命周期**：`start`/`run`/`join`(×3)/`sleep`(×2)/`interrupt`/`interrupted`/`isInterrupted`/`yield`。
- **静态工具**：`currentThread`、`activeCount`、`enumerate`、`holdsLock`、`dumpStack`、
  `getDefaultUncaughtExceptionHandler/setDefaultUncaughtExceptionHandler`。
- **其他**：`getStackTrace`、`checkAccess`、`toString`、`setContextClassLoader/getContextClassLoader`、
  `setUncaughtExceptionHandler/getUncaughtExceptionHandler`、`clone`（protected，恒抛异常）。

## 2. Java 用户如何使用

- 新建线程并 `start()`；用 `join()` 等待结束（对已终止线程立即返回）；`sleep` 限时阻塞；
  `interrupt` 设置中断标志（对 sleep/join 抛 `InterruptedException`）。
- `setDaemon(true)` 须在 `start()` 之前；`start()` 后设置 daemon 或重复 `start()` → `IllegalThreadStateException`。
- `setPriority` 仅接受 `[MIN_PRIORITY, MAX_PRIORITY]`，越界 → `IllegalArgumentException`。
- 构造器 `name == null` → `NullPointerException`；`ThreadGroup == null` → 用当前线程的 group；
  `Runnable == null` 合法（run() 为空）。
- daemon 标志默认继承自构造线程。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Thread.html`）：构造器/异常契约、null 语义、
  `setDaemon`/`start` 的 ITSE、`setPriority` 的 IAE、`join`/`sleep` 的 InterruptedException。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Thread.java`：
  `getId` 非 final、`getDefaultUncaughtExceptionHandler` 默认 null、`setDaemon` 仅在线程存活时抛 ITSE。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Thread` + `~/git/jdk/test/jdk/java/lang/Thread`：
  构造器/daemon/优先级/join 历史缺陷回归。

## 4. 测试用例设计（映射到 ThreadTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：类非 final、实现 Runnable、currentThread native、sleep/activeCount static | testWhiteBox |
| P0 | 当前线程字段（name/id/priority/daemon/group/state/contextClassLoader） | testCurrentAndFields |
| P0 | setPriority 边界（MIN/MAX/恢复）+ 0/11 → IAE | testPrioritySetter |
| P0 | 默认名字前缀/去重/命名构造器 | testNames |
| P0 | 构造器矩阵：null name→NPE、null group→父 group、null Runnable 合法、daemon 继承 | testConstructorMatrix |
| P0 | start/run/join/双重 start→ITSE/join 已终止线程立即返回 | testStartRunJoin |
| P0 | 直接 run() 在当前线程执行且不改变状态 | testRunDirect |
| P0 | setDaemon after start → ITSE（用闩锁保持线程存活，消除竞态） | testDaemonAfterStart |
| P1 | sleep 至少阻塞请求时长 | testSleep |
| P1 | interrupt/isInterrupted/interrupted 标志语义 | testInterruptFlag |
| P1 | sleep 被 interrupt → InterruptedException | testSleepInterrupted |
| P1 | holdsLock | testHoldsLock |
| P1 | uncaught handler（实例默认非 null、设置回读、全局默认 null） | testUncaughtHandler |
| P1 | activeCount/enumerate/getStackTrace/dumpStack/checkAccess/toString/contextClassLoader/ThreadGroup 构造器/4 参构造器/clone 恒抛异常 | testIntrospectionAndGroups |

## 关键规范细节（易错，已锁入断言）

- **`getDefaultUncaughtExceptionHandler()` 默认返回 `null`**（仅 `setDefaultUncaughtExceptionHandler` 后非 null）。
- **`setDaemon` 仅在线程存活（`isAlive()`）时抛 ITSE**：测试须用闩锁（CountDownLatch）保持线程存活再断言，
  避免空 run() 线程先终止导致不抛异常。
- **`Thread(ThreadGroup,null,Runnable,String,long)` 5 参 protected 构造器是 Java 21+，JDK-8 无**。
- **`getId` 在 JDK-8 非 final**（final 是后来加的）。
- **`clone()` protected 且恒抛 `CloneNotSupportedException`**（Thread 不实现 Cloneable）。

## 注意事项

- 所有辅助线程设 daemon；join/sleep 用有界超时或确定性闩锁，不依赖 sleep 时序/墙钟。
- 不修改全局状态（除临时 setDefaultUncaughtExceptionHandler 并在 finally 恢复外不设全局）；
  `dumpStack` 写 stderr 属预期。
- headless 安全，不加 `@key`。
