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

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **状态机精确断言**：NEW→start→RUNNABLE→（untimed `CountDownLatch.await()` 恒 WAITING、
  `await(10s)` 恒 TIMED_WAITING、争抢他人 monitor 恒 BLOCKED、`Object.wait()` WAITING）→TERMINATED；
  终止后 getState 恒 TERMINATED。全部经 `awaitState()` 10s 有界轮询，无时序强断言。
- **双重 start**：存活线程第二次 start→ITSE；直接调 run() 后仍可 start（状态未变）。
- **interrupt 矩阵**：NEW 线程 interrupt 后 8 不可观测（无标志可见）；TERMINATED 同；
  存活 worker 在 sleep/await 中被中断 → IE 且 catch 内 `isInterrupted()==false`（标志被清除）；
  `interrupted()` 消费一次、`isInterrupted()` 不清除；wait/join/sleep 自中断立即抛 IE。
- **UncaughtExceptionHandler 路由**：per-thread handler（identity：thread==、throwable==同一实例）>
  ThreadGroup.uncaughtException > Thread 默认 handler；默认 handler 初始 null；
  normal 退出不触发任何 handler；全局设置用 try/finally 还原。
- **优先级钳制**：setPriority 越界→IAE（`timeout`/`priority` 消息文本不锁）；线程组 max 钳制
  初始 priority 与后续 setPriority（组 max 9 时 setPriority(10)→9）；死线程 setPriority 在 8 上 no-op 保持 5。
- **join/sleep 参数负例**：join(-1) IAE "timeout value is negative"；join(0,-1)/(0,1000000) IAE
  "nanosecond timeout value out of range"；sleep 同形。join(80) 实测 ≥80ms。
- **toString 精确**：`"Thread[ts-name,5,ts-group]"`（8 无 #id 前缀）；死后 group 段为空 `"Thread[ts-renamed,7,]"`。
- **NPE 精确消息**：构造器与 setName(null) 均为 `"name cannot be null"`（8 构造器与 setName 同消息）。
- **holdsLock**：自持 true、释放后 false、他人锁 false、null→NPE（消息为 null，只锁类型）。

### 使用方式
latch 同步（entered/release + untimed await）作状态锚点；`awaitState`/`awaitActiveCount` 有界轮询；
所有子线程 setDaemon(true) 且 release 在 finally；finally 还原默认 handler/优先级/组。

### 参考来源
双侧实测（Temurin 8u372 / 27.0.1）：状态锚点、IE 消息、优先级钳制、toString 格式差异均经 /tmp 探针验证后锁入。

### 测试用例设计 + 易错规范细节
- 新增 ~40 个 testXxx：testStateMachineDeterministic/testTimedWaitingState/testBlockedState/
  testDoubleStartLive/testDaemonLifecycle/testPriorityFull/testInterruptLatchedWorker/
  testUncaughtPrecedence/testJoinForms/testToStringPrecise 等（67→281 断言）。
- 易错点：**untimed await 恒 WAITING、timed await 恒 TIMED_WAITING**（断言 WAITING 时 worker 必须用 untimed）；
  8 上死后 setPriority no-op（27 会生效）；8 上 NEW/TERMINATED 线程的 interrupt 标志不可从外部观测。

- 易错点（jtreg 环境坑）：**默认 handler 只在线程组链到达未被改写的根组时被咨询**；jtreg 把测试 main 包在
  自有 MainThreadGroup（覆写 uncaughtException 且不委托）里，子线程死异常会被 harness 截走并判负。
  因此 default-routing 用例的 worker 必须锚定真实根组（getParent() 走到头），与运行环境无关。
